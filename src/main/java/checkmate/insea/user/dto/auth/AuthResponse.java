package checkmate.insea.user.dto.auth;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo userInfo;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long userId;
        private String email;
        private String nickname;
        private String profileImageUrl;
        private Integer userLevel;
        private Integer totalCatches;
        private String deviceType;
    }
}
