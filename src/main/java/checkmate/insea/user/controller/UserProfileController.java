package checkmate.insea.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import checkmate.insea.user.dto.profile.UserProfileDto;
import checkmate.insea.user.model.User;
import checkmate.insea.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@CrossOrigin(origins="*", maxAge = 3600)
public class UserProfileController {
    
    private final UserProfileService userProfileService;
    
    /**
     * 닉네임 변경
     */
    @PutMapping("/nickname")
    public ResponseEntity<UserProfileDto.GeneralResponse> changeNickname(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserProfileDto.ChangeNicknameRequest request) {
        
        if (user == null) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }
        
        log.info("닉네임 변경 API 호출 - 사용자: {}, 요청닉네임: {}", user.getUserId(), request.getNewNickname());
        
        UserProfileDto.GeneralResponse response = userProfileService.changeNickname(user, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이메일 변경
     */
    @PutMapping("/email")
    public ResponseEntity<UserProfileDto.GeneralResponse> changeEmail(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserProfileDto.ChangeEmailRequest request) {
        
        if (user == null) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }
        
        log.info("이메일 변경 API 호출 - 사용자: {}, 요청이메일: {}", user.getUserId(), request.getNewEmail());
        
        UserProfileDto.GeneralResponse response = userProfileService.changeEmail(user, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 비밀번호 변경
     */
    @PutMapping("/password")
    public ResponseEntity<UserProfileDto.GeneralResponse> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserProfileDto.ChangePasswordRequest request) {
        
        if (user == null) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }
        
        log.info("비밀번호 변경 API 호출 - 사용자: {}", user.getUserId());
        
        UserProfileDto.GeneralResponse response = userProfileService.changePassword(user, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 프로필 이미지 변경 (Cloudinary 사용)
     */
    @PostMapping("/image")
    public ResponseEntity<UserProfileDto.ProfileImageResponse> changeProfileImage(
            @AuthenticationPrincipal User user,
            @RequestParam("image") MultipartFile imageFile) {
        
        if (user == null) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }
        
        log.info("프로필 이미지 변경 API 호출 - 사용자: {}, 파일명: {}, 크기: {}bytes", 
                user.getUserId(), 
                imageFile != null ? imageFile.getOriginalFilename() : "null",
                imageFile != null ? imageFile.getSize() : 0);
        
        try {
            UserProfileDto.ProfileImageResponse response = userProfileService.changeProfileImage(user, imageFile);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("프로필 이미지 변경 실패 - 사용자: {}, 오류: {}", user.getUserId(), e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 프로필 이미지 삭제 (Cloudinary에서 삭제)
     */
    @DeleteMapping("/image")
    public ResponseEntity<UserProfileDto.GeneralResponse> removeProfileImage(
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }
        
        log.info("프로필 이미지 삭제 API 호출 - 사용자: {}", user.getUserId());
        
        UserProfileDto.GeneralResponse response = userProfileService.removeProfileImage(user);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 프로필 썸네일 URL 조회
     */
    @GetMapping("/image/thumbnail")
    public ResponseEntity<UserProfileDto.ThumbnailResponse> getProfileThumbnail(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "width", defaultValue = "150") int width,
            @RequestParam(value = "height", defaultValue = "150") int height) {
        
        if (user == null) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }
        
        String thumbnailUrl = userProfileService.getProfileThumbnailUrl(user, width, height);
        
        UserProfileDto.ThumbnailResponse response = UserProfileDto.ThumbnailResponse.builder()
                .thumbnailUrl(thumbnailUrl)
                .width(width)
                .height(height)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/account")
    public ResponseEntity<UserProfileDto.GeneralResponse> deleteAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserProfileDto.DeleteAccountRequest request) {
        
        if (user == null) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }

        log.info("회원 탈퇴 API 호출 - 사용자: {}, 닉네임 확인: {}, 사유: {}",
                user.getUserId(), request.getNicknameConfirm(), request.getReason());

        UserProfileDto.GeneralResponse response = userProfileService.deleteAccount(user, request);
        return ResponseEntity.ok(response);
    }
}