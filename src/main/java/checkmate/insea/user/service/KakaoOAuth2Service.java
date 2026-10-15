package checkmate.insea.user.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import checkmate.insea.user.dto.auth.AuthResponse;
import checkmate.insea.user.dto.kakao.KakaoLoginRequest;
import checkmate.insea.user.dto.kakao.KakaoTokenResponse;
import checkmate.insea.user.dto.kakao.KakaoUserInfo;
import checkmate.insea.user.exception.AuthException;
import checkmate.insea.user.model.User;
import checkmate.insea.user.model.UserAuthProvider;
import checkmate.insea.user.repository.UserAuthProviderRepository;
import checkmate.insea.user.repository.UserRepository;
import checkmate.insea.user.security.JwtUtil;
import checkmate.insea.weather.model.Location;
import checkmate.insea.weather.model.UserLocationSetting;
import checkmate.insea.weather.repository.LocationRepository;
import checkmate.insea.weather.repository.UserLocationSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KakaoOAuth2Service {
    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${oauth2.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final WebClient webClient;
    private final UserRepository userRepository;
    private final UserAuthProviderRepository authProviderRepository;
    private final JwtUtil jwtUtil;

    private final LocationRepository locationRepository;
    private final UserLocationSettingRepository userLocationSettingRepository;

    public String getKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&response_type=code"
                + "&scope=profile_nickname,profile_image,account_email";
    }

    public AuthResponse kakaoLogin(KakaoLoginRequest request) {
        log.info("Starting Kakao OAuth2 login process");

        try {
            // 1. 카카오 액세스 토큰 획득
            KakaoTokenResponse tokenResponse = getKakaoAccessToken(request.getCode());

            // 2. 카카오 사용자 정보 획득
            KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(tokenResponse.getAccessToken());

            // 3. 사용자 찾기 또는 생성
            User user = findOrCreateUser(kakaoUserInfo, tokenResponse, request);

            // 4. JWT 토큰 생성 및 반환
            return generateAuthResponse(user);

        } catch (Exception e) {
            log.error("Kakao login failed", e);
            throw new AuthException("카카오 로그인에 실패했습니다");
        }
    }

    private KakaoTokenResponse getKakaoAccessToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", authorizationCode);

        return webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> {
                            log.error("Failed to get Kakao access token. Status: {}", response.statusCode());
                            return response.bodyToMono(String.class)
                                    .map(body -> new AuthException("카카오 토큰 획득 실패: " + body));
                        })
                .bodyToMono(KakaoTokenResponse.class)
                .block();
    }

    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> {
                            log.error("Failed to get Kakao user info. Status: {}", response.statusCode());
                            return response.bodyToMono(String.class)
                                    .map(body -> new AuthException("카카오 사용자 정보 획득 실패: " + body));
                        })
                .bodyToMono(KakaoUserInfo.class)
                .block();
    }

    private User findOrCreateUser(KakaoUserInfo kakaoUserInfo, KakaoTokenResponse tokenResponse, KakaoLoginRequest request) {
        String kakaoId = String.valueOf(kakaoUserInfo.getId());

        // 기존 카카오 연동 계정 확인
        Optional<UserAuthProvider> existingAuthProvider =
                authProviderRepository.findByProviderAndProviderUserId("kakao", kakaoId);

        if (existingAuthProvider.isPresent()) {
            // 기존 사용자 - 토큰 정보 업데이트
            return updateExistingKakaoUser(existingAuthProvider.get(), kakaoUserInfo, tokenResponse, request);
        } else {
            String email = kakaoUserInfo.getKakaoAccount().getEmail();

            // 이메일로 기존 사용자 확인
            Optional<User> existingUser = userRepository.findByEmail(email);

            if (existingUser.isPresent()) {
                // 이메일로 가입된 기존 사용자 - 카카오 연동 추가
                return linkKakaoToExistingUser(existingUser.get(), kakaoUserInfo, tokenResponse, request);
            } else {
                // 완전히 새로운 사용자 생성
                return createNewKakaoUser(kakaoUserInfo, tokenResponse, request);
            }
        }
    }

    private User updateExistingKakaoUser(UserAuthProvider authProvider, KakaoUserInfo kakaoUserInfo, KakaoTokenResponse tokenResponse, KakaoLoginRequest request) {
        User user = authProvider.getUser();

        // 비활성(탈퇴) 상태인 경우, 계정을 다시 활성화하고 정보를 업데이트
        if (!user.getIsActive()) {
            log.info("탈퇴했던 사용자(ID: {})가 재로그인을 시도하여 계정을 활성화합니다.", user.getUserId());
            user.setIsActive(true);
            user.setEmail(kakaoUserInfo.getKakaoAccount().getEmail());
            // 닉네임이 중복될 수 있으므로 unique 닉네임 생성 로직 재사용
            user.setNickname(generateUniqueNickname(kakaoUserInfo.getProperties().getNickname()));
            user.setProfileImageUrl(kakaoUserInfo.getProperties().getProfileImage());
            user.setUpdatedAt(LocalDateTime.now());
        }

        // 카카오 인증 정보 업데이트
        authProvider.setAccessToken(tokenResponse.getAccessToken());
        authProvider.setRefreshToken(tokenResponse.getRefreshToken());
        authProvider.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
        authProvider.setUpdatedAt(LocalDateTime.now());
        authProviderRepository.save(authProvider);

        // 사용자 로그인 정보 업데이트
        user.setLastLoginDate(LocalDateTime.now());
        if (request.getFcmToken() != null) {
            user.setFcmToken(request.getFcmToken());
        }
        if (request.getDeviceType() != null) {
            user.setDeviceType(request.getDeviceType());
        }

        return userRepository.save(user);
    }

    private User linkKakaoToExistingUser(User existingUser, KakaoUserInfo kakaoUserInfo,
                                         KakaoTokenResponse tokenResponse, KakaoLoginRequest request) {
        
        // 이메일로 찾은 사용자가 비활성 상태일 경우, 재활성화
        if (!existingUser.getIsActive()) {
            log.info("탈퇴했던 이메일 사용자(ID: {})에게 카카오 계정을 연동하며 계정을 활성화합니다.", existingUser.getUserId());
            existingUser.setIsActive(true);
            // 필요하다면 카카오 정보로 닉네임/프로필 업데이트
            existingUser.setNickname(generateUniqueNickname(kakaoUserInfo.getProperties().getNickname()));
            existingUser.setProfileImageUrl(kakaoUserInfo.getProperties().getProfileImage());
        }
        
        // 기존 사용자에 카카오 연동 추가
        UserAuthProvider authProvider = UserAuthProvider.builder()
                .user(existingUser)
                .provider("kakao")
                .providerUserId(String.valueOf(kakaoUserInfo.getId()))
                .providerEmail(kakaoUserInfo.getKakaoAccount().getEmail())
                .providerNickname(kakaoUserInfo.getProperties().getNickname())
                .providerProfileImage(kakaoUserInfo.getProperties().getProfileImage())
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()))
                .build();

        authProviderRepository.save(authProvider);

        // 사용자 정보 업데이트
        existingUser.setLastLoginDate(LocalDateTime.now());
        if (request.getFcmToken() != null) {
            existingUser.setFcmToken(request.getFcmToken());
        }
        if (request.getDeviceType() != null) {
            existingUser.setDeviceType(request.getDeviceType());
        }

        return userRepository.save(existingUser);
    }

    private User createNewKakaoUser(KakaoUserInfo kakaoUserInfo, KakaoTokenResponse tokenResponse, KakaoLoginRequest request) {
        String email = kakaoUserInfo.getKakaoAccount().getEmail();
        String nickname = generateUniqueNickname(kakaoUserInfo.getProperties().getNickname());

        // 새 사용자 생성
        User newUser = User.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl(kakaoUserInfo.getProperties().getProfileImage())
                .fcmToken(request.getFcmToken())
                .deviceType(request.getDeviceType())
                .lastLoginDate(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);

        // 카카오 인증 정보 저장
        UserAuthProvider authProvider = UserAuthProvider.builder()
                .user(savedUser)
                .provider("kakao")
                .providerUserId(String.valueOf(kakaoUserInfo.getId()))
                .providerEmail(email)
                .providerNickname(kakaoUserInfo.getProperties().getNickname())
                .providerProfileImage(kakaoUserInfo.getProperties().getProfileImage())
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()))
                .build();

        authProviderRepository.save(authProvider);
        setDefaultLocationForNewUser(savedUser);

        return savedUser;
    }

    private void setDefaultLocationForNewUser(User newUser) {
        // 기본값으로 설정할 Location 엔티티를 DB에서 조회합니다 (ID: 1L).
        Location defaultLocation = locationRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("기본 위치(ID: 1)를 찾을 수 없습니다. 데이터 초기화를 확인해주세요."));

        // 새로운 UserLocationSetting 객체를 생성합니다.
        UserLocationSetting newSetting = new UserLocationSetting(newUser);

        // 조회한 Location을 기본 위치로 설정합니다.
        newSetting.setDefaultLocation(defaultLocation);

        // 설정 정보를 DB에 저장합니다.
        userLocationSettingRepository.save(newSetting);
        log.info("신규 카카오 사용자(ID: {})에게 기본 위치(ID: 1) 설정.", newUser.getUserId());
    }

    private String generateUniqueNickname(String baseNickname) {
        if (baseNickname == null || baseNickname.trim().isEmpty()) {
            baseNickname = "카카오사용자";
        }

        String uniqueNickname = baseNickname;
        int counter = 1;

        while (userRepository.existsByNickname(uniqueNickname)) {
            uniqueNickname = baseNickname + counter++;
        }

        return uniqueNickname;
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getUserId());

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .userLevel(user.getUserLevel())
                .totalCatches(user.getTotalCatches())
                .deviceType(user.getDeviceType())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L)
                .userInfo(userInfo)
                .build();
    }
}
