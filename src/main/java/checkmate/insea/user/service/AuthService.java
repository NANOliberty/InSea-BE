package checkmate.insea.user.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import checkmate.insea.user.dto.auth.AuthResponse;
import checkmate.insea.user.dto.auth.LoginRequest;
import checkmate.insea.user.dto.auth.SignUpRequest;
import checkmate.insea.user.dto.auth.TokenRefreshRequest;
import checkmate.insea.user.dto.auth.TokenRefreshResponse;
import checkmate.insea.user.exception.AuthException;
import checkmate.insea.user.model.User;
import checkmate.insea.user.repository.UserRepository;
import checkmate.insea.user.security.JwtUtil;
import checkmate.insea.user.security.UserPrincipal;
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
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    private final LocationRepository locationRepository;
    private final UserLocationSettingRepository userLocationSettingRepository;

    public AuthResponse signUp(SignUpRequest request) {
        log.info("Starting user registration for email: {}", request.getEmail());

        // 중복 검사
        validateUserDoesNotExist(request.getEmail(), request.getNickname());

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl(request.getProfileImageUrl())
                .fcmToken(request.getFcmToken())
                .deviceType(request.getDeviceType())
                .build();

        User savedUser = userRepository.save(user);
        setDefaultLocationForNewUser(savedUser);
        log.info("User registered successfully with ID: {}", savedUser.getUserId());

        // JWT 토큰 생성
        return generateAuthResponse(savedUser);
    }

    /**
     * 신규 가입한 사용자의 기본 위치 설정을 생성하고 저장
     * @param newUser 새로 가입한 User 엔티티
     */
    private void setDefaultLocationForNewUser(User newUser) {
        // 1. 기본값으로 설정할 Location 엔티티를 DB에서 조회합니다 (ID: 1L).
        // DataInitializer에 의해 이 위치는 항상 존재해야 합니다.
        Location defaultLocation = locationRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("기본 위치(ID: 1)를 찾을 수 없습니다. 데이터 초기화를 확인해주세요."));

        // 2. 새로운 UserLocationSetting 객체를 생성
        UserLocationSetting newSetting = new UserLocationSetting(newUser);

        // 3. 조회한 Location을 기본 위치로 설정
        newSetting.setDefaultLocation(defaultLocation);

        // 4. 설정 정보를 DB에 저장
        userLocationSettingRepository.save(newSetting);
        log.info("신규 사용자(ID: {})에게 기본 위치(ID: 1) 설정.", newUser.getUserId());

    }

    public AuthResponse login(LoginRequest request) {
        log.info("Starting user login for email: {}", request.getEmail());

        try {
            // Spring Security 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();

            // 로그인 정보 업데이트
            updateLoginInfo(user, request.getFcmToken(), request.getDeviceType());

            log.info("User logged in successfully: {}", user.getEmail());

            return generateAuthResponse(user);

        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {}", request.getEmail());
            throw new AuthException("이메일 또는 비밀번호가 올바르지 않습니다");
        }
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new AuthException("유효하지 않은 리프레시 토큰입니다");
        }

        String email = jwtUtil.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new AuthException("사용자를 찾을 수 없습니다"));

        // 새 토큰 생성
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getUserId());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(3600L) // 1시간
                .build();
    }

    public void logout(String email) {
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new AuthException("사용자를 찾을 수 없습니다"));

        // FCM 토큰 제거 (푸시 알림 중단)
        user.setFcmToken(null);
        userRepository.save(user);

        log.info("User logged out: {}", email);
    }

    private void validateUserDoesNotExist(String email, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new AuthException("이미 존재하는 이메일입니다");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new AuthException("이미 존재하는 닉네임입니다");
        }
    }

    private void updateLoginInfo(User user, String fcmToken, String deviceType) {
        user.setLastLoginDate(LocalDateTime.now());
        if (fcmToken != null) {
            user.setFcmToken(fcmToken);
        }
        if (deviceType != null) {
            user.setDeviceType(deviceType);
        }
        userRepository.save(user);
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
                .expiresIn(3600L) // 1시간
                .userInfo(userInfo)
                .build();
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isNicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

}
