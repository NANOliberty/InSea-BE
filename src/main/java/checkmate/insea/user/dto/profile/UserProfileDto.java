package checkmate.insea.user.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UserProfileDto {
    
    /**
     * 닉네임 변경 요청
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangeNicknameRequest {
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        private String currentPassword;
        
        @NotBlank(message = "새 닉네임을 입력해주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9_]+$", message = "닉네임은 한글, 영문, 숫자, 언더스코어만 사용 가능합니다.")
        private String newNickname;
    }
    
    /**
     * 이메일 변경 요청
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangeEmailRequest {
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        private String currentPassword;
        
        @NotBlank(message = "새 이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String newEmail;
        
        @NotBlank(message = "새 이메일의 비밀번호 확인을 입력해주세요.")
        private String newEmailPasswordConfirm;
    }
    
    /**
     * 비밀번호 변경 요청
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangePasswordRequest {
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        private String currentPassword;
        
        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하로 입력해주세요.")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
                message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.")
        private String newPassword;
        
        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        private String newPasswordConfirm;
    }
    
    /**
     * 프로필 이미지 변경 응답
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileImageResponse {
        private String profileImageUrl;
        private String message;
        
        public static ProfileImageResponse of(String profileImageUrl, String message) {
            ProfileImageResponse response = new ProfileImageResponse();
            response.profileImageUrl = profileImageUrl;
            response.message = message;
            return response;
        }
    }
    
    /**
     * 프로필 이미지 정보 응답
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileImageInfo {
        private boolean hasImage;           // 프로필 이미지 존재 여부
        private String originalUrl;         // 원본 이미지 URL
        private String thumbnailUrl;        // 썸네일 URL (150x150)
        private String largeUrl;           // 큰 이미지 URL (800x800)
        
        public static ProfileImageInfo noImage() {
            ProfileImageInfo info = new ProfileImageInfo();
            info.hasImage = false;
            info.originalUrl = null;
            info.thumbnailUrl = null;
            info.largeUrl = null;
            return info;
        }
    }
    
    /**
     * 회원 탈퇴 요청
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeleteAccountRequest {
        @NotBlank(message = "닉네임을 입력해주세요.")
        private String nicknameConfirm;
        
        @NotBlank(message = "탈퇴 사유를 선택해주세요.")
        private String reason;
        
        private String additionalReason; // 기타 사유
    }
    
    /**
     * 일반 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneralResponse {

        public static GeneralResponse success(String message) {
            return GeneralResponse.builder()
                    .success(true)
                    .message(message)
                    .build();
        }

        public static GeneralResponse failure(String message) {
            return GeneralResponse.builder()
                    .success(false)
                    .message(message)
                    .build();
        }
        
        private boolean success;
        private String message;
    }
    // UserProfileDto.java에 추가할 ThumbnailResponse 클래스

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThumbnailResponse {
        private String thumbnailUrl;
        private int width;
        private int height;
    }
}