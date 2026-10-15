package checkmate.insea.user.controller;

import checkmate.insea.user.dto.auth.*;
import checkmate.insea.user.dto.kakao.KakaoLoginRequest;
import checkmate.insea.user.model.User;
import checkmate.insea.user.service.AuthService;
import checkmate.insea.user.service.KakaoOAuth2Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins="*", maxAge = 3600)
public class AuthController {
    private final AuthService authService;
    private final KakaoOAuth2Service kakaoOAuth2Service;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("Signup request received for email: {}", request.getEmail());
        AuthResponse response = authService.signUp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token refresh request received");
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal User user) {
        log.info("Logout request received for user: {}", user.getEmail());
        authService.logout(user.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("message", "로그아웃되었습니다");
        return ResponseEntity.ok(response);
    }

    // 카카오 로그인 URL 제공
    @GetMapping("/kakao/login-url")
    public ResponseEntity<Map<String, String>> getKakaoLoginUrl() {
        String loginUrl = kakaoOAuth2Service.getKakaoLoginUrl();

        Map<String, String> response = new HashMap<>();
        response.put("loginUrl", loginUrl);

        return ResponseEntity.ok(response);
    }

    // 카카오 로그인 처리
    @PostMapping("/kakao/login")
    public ResponseEntity<AuthResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        log.info("Kakao login request received");
        AuthResponse response = kakaoOAuth2Service.kakaoLogin(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kakao/failure")
    public ResponseEntity<Map<String, String>> kakaoLoginFailure(@RequestParam(required = false) String error) {
        log.warn("Kakao OAuth2 로그인 실패: {}", error);
        Map<String, String> response = new HashMap<>();
        response.put("error", "Kakao 로그인 실패");
        if (error != null) {
            response.put("reason", error);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 현재 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser(@AuthenticationPrincipal User user) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .userLevel(user.getUserLevel())
                .totalCatches(user.getTotalCatches())
                .deviceType(user.getDeviceType())
                .build();

        return ResponseEntity.ok(userInfo);
    }

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = !authService.isEmailExists(email);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);

        return ResponseEntity.ok(response);
    }

    // 닉네임 중복 확인
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNicknameAvailability(@RequestParam String nickname) {
        boolean isAvailable = !authService.isNicknameExists(nickname);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);

        return ResponseEntity.ok(response);
    }
}
