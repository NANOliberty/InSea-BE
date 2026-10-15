package checkmate.insea.user.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import checkmate.insea.image.service.CloudinaryService;
import checkmate.insea.user.dto.profile.UserProfileDto;
import checkmate.insea.user.model.User;
import checkmate.insea.user.repository.UserFavoriteRepository;
import checkmate.insea.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {
    
    private final UserRepository userRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService; // Cloudinary 서비스 추가
    
    /**
     * 닉네임 변경
     */
    @Transactional
    public UserProfileDto.GeneralResponse changeNickname(User user, UserProfileDto.ChangeNicknameRequest request) {
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNewNickname())) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }
        
        // 현재 닉네임과 동일한지 확인
        if (user.getNickname().equals(request.getNewNickname())) {
            throw new RuntimeException("현재 닉네임과 동일합니다.");
        }
        
        // 닉네임 업데이트
        user.setNickname(request.getNewNickname());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("사용자 {}의 닉네임이 {}로 변경되었습니다.", user.getUserId(), request.getNewNickname());
        
        return UserProfileDto.GeneralResponse.builder()
                .success(true)
                .message("닉네임이 성공적으로 변경되었습니다.")
                .build();
    }
    
    /**
     * 이메일 변경
     */
    @Transactional
    public UserProfileDto.GeneralResponse changeEmail(User user, UserProfileDto.ChangeEmailRequest request) {
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 이메일 비밀번호 확인 검증
        if (!passwordEncoder.matches(request.getNewEmailPasswordConfirm(), user.getPassword())) {
            throw new RuntimeException("비밀번호 확인이 일치하지 않습니다.");
        }
        
        // 새 이메일 중복 확인
        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        
        // 현재 이메일과 동일한지 확인
        if (user.getEmail().equals(request.getNewEmail())) {
            throw new RuntimeException("현재 이메일과 동일합니다.");
        }
        
        // 이메일 업데이트
        user.setEmail(request.getNewEmail());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("사용자 {}의 이메일이 {}로 변경되었습니다.", user.getUserId(), request.getNewEmail());
        
        return UserProfileDto.GeneralResponse.builder()
                .success(true)
                .message("이메일이 성공적으로 변경되었습니다.")
                .build();
    }
    
    /**
     * 비밀번호 변경
     */
    @Transactional
    public UserProfileDto.GeneralResponse changePassword(User user, UserProfileDto.ChangePasswordRequest request) {
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 비밀번호와 확인 비밀번호 일치 확인
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new RuntimeException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        
        // 현재 비밀번호와 새 비밀번호가 같은지 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.");
        }
        
        // 비밀번호 업데이트
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("사용자 {}의 비밀번호가 변경되었습니다.", user.getUserId());
        
        return UserProfileDto.GeneralResponse.builder()
                .success(true)
                .message("비밀번호가 성공적으로 변경되었습니다.")
                .build();
    }
    
    /**
     * 프로필 이미지 변경 (Cloudinary 사용)
     */
    @Transactional
    public UserProfileDto.ProfileImageResponse changeProfileImage(User user, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new RuntimeException("이미지 파일을 선택해주세요.");
        }
        
        try {
            // 기존 프로필 이미지 삭제 (Cloudinary에서)
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                boolean deleted = cloudinaryService.deleteProfileImage(user.getUserId());
                if (deleted) {
                    log.info("기존 프로필 이미지 삭제 완료 - 사용자: {}", user.getUserId());
                }
            }
            
            // Cloudinary에 새 프로필 이미지 업로드
            String imageUrl = cloudinaryService.uploadProfileImage(imageFile, user.getUserId());
            
            // 프로필 이미지 URL 업데이트
            user.setProfileImageUrl(imageUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("사용자 {}의 프로필 이미지가 변경되었습니다: {}", user.getUserId(), imageUrl);
            
            return UserProfileDto.ProfileImageResponse.builder()
                    .profileImageUrl(imageUrl)
                    .message("프로필 이미지가 성공적으로 변경되었습니다.")
                    .build();
                    
        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("프로필 이미지 변경 실패: {}", e.getMessage(), e);
            throw new RuntimeException("프로필 이미지 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 프로필 이미지 삭제 (Cloudinary에서 삭제)
     */
    @Transactional
    public UserProfileDto.GeneralResponse removeProfileImage(User user) {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            // Cloudinary에서 이미지 삭제
            boolean deleted = cloudinaryService.deleteProfileImage(user.getUserId());
            if (!deleted) {
                log.warn("Cloudinary에서 프로필 이미지 삭제 실패 - 사용자: {}", user.getUserId());
            }
        }
        
        // DB에서 프로필 이미지 URL 제거
        user.setProfileImageUrl(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("사용자 {}의 프로필 이미지가 삭제되었습니다.", user.getUserId());
        
        return UserProfileDto.GeneralResponse.builder()
                .success(true)
                .message("프로필 이미지가 삭제되었습니다.")
                .build();
    }
    
    /**
     * 회원 탈퇴
     */
    @Transactional
    public UserProfileDto.GeneralResponse deleteAccount(User user, UserProfileDto.DeleteAccountRequest request) {
        // 현재 비밀번호 확인
        if (!user.getNickname().equals(request.getNicknameConfirm())) {
            throw new RuntimeException("닉네임이 일치하지 않습니다. 정확한 닉네임을 입력해주세요.");
        }

        try {
            // 1. 사용자의 즐겨찾기 삭제
            userFavoriteRepository.deleteAllByUserId(user.getUserId());
            
            // 2. Cloudinary에서 프로필 이미지 삭제
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                boolean deleted = cloudinaryService.deleteProfileImage(user.getUserId());
                if (deleted) {
                    log.info("회원 탈퇴 - 프로필 이미지 삭제 완료: {}", user.getUserId());
                } else {
                    log.warn("회원 탈퇴 - 프로필 이미지 삭제 실패: {}", user.getUserId());
                }
            }
            
            // 3. 사용자 계정 비활성화 (실제 삭제 대신 비활성화)
            user.setIsActive(false);
            user.setEmail(user.getEmail() + "_deleted_" + System.currentTimeMillis());
            user.setNickname(user.getNickname() + "_deleted_" + System.currentTimeMillis());
            user.setProfileImageUrl(null); // 프로필 이미지 URL 제거
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            // 탈퇴 로그 기록
            log.info("사용자 {} 계정이 탈퇴 처리되었습니다. 사유: {}", user.getUserId(), request.getReason());
            
            return UserProfileDto.GeneralResponse.success("회원 탈퇴가 완료되었습니다.");
                    
        } catch (Exception e) {
            log.error("회원 탈퇴 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 프로필 썸네일 URL 생성
     */
    public String getProfileThumbnailUrl(User user, int width, int height) {
        if (user.getProfileImageUrl() == null || user.getProfileImageUrl().isEmpty()) {
            return null;
        }
        
        return cloudinaryService.generateThumbnailUrl(user.getUserId(), width, height);
    }
}