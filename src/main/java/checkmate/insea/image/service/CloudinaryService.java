package checkmate.insea.image.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {
    
    private final Cloudinary cloudinary;
    
    /**
     * 단일 이미지 업로드
     */
    public String uploadImage(MultipartFile file, String folder) {
        try {
            // 파일 유효성 검사
            validateImageFile(file);
            
            // 고유한 파일명 생성
            String publicId = generatePublicId(folder, file.getOriginalFilename());
            
            // Cloudinary 업로드 옵션
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "image",
                "quality", "auto:good",
                "fetch_format", "auto"
            );
            
            // 업로드 실행
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader()
                .upload(file.getBytes(), uploadOptions);
            
            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully: {}", imageUrl);
            
            return imageUrl;
            
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 다중 이미지 업로드
     */
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        List<String> imageUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String imageUrl = uploadImage(file, folder);
                imageUrls.add(imageUrl);
            }
        }
        
        return imageUrls;
    }
    
    /**
     * 이미지 삭제
     */
    public boolean deleteImage(String imageUrl) {
        try {
            // URL에서 public_id 추출
            String publicId = extractPublicIdFromUrl(imageUrl);
            
            if (publicId != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> deleteResult = cloudinary.uploader()
                    .destroy(publicId, ObjectUtils.emptyMap());
                
                String result = (String) deleteResult.get("result");
                boolean success = "ok".equals(result);
                
                log.info("Image deletion {}: {}", success ? "successful" : "failed", imageUrl);
                return success;
            }
            
            return false;
            
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary: {}", imageUrl, e);
            return false;
        }
    }
    
    /**
     * 다중 이미지 삭제
     */
    public void deleteImages(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                deleteImage(imageUrl);
            }
        }
    }
    
    /**
     * 파일 유효성 검사
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        
        // 파일 크기 제한 (5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        
        // 파일 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        
        // 허용된 확장자 검사
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "webp");
            
            if (!allowedExtensions.contains(extension)) {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
            }
        }
    }
    
    /**
     * 고유한 public_id 생성
     */
    private String generatePublicId(String folder, String originalFilename) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        String filename = originalFilename != null ? 
            originalFilename.substring(0, originalFilename.lastIndexOf(".")) : "image";
        
        return folder + "/" + filename + "_" + timestamp + "_" + uuid;
    }
    
    /**
     * Cloudinary URL에서 public_id 추출
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // Cloudinary URL 패턴: https://res.cloudinary.com/cloud-name/image/upload/v1234567890/folder/filename.jpg
            if (imageUrl.contains("cloudinary.com")) {
                String[] parts = imageUrl.split("/");
                if (parts.length >= 7) {
                    // v1234567890 이후 부분이 public_id (확장자 제거)
                    StringBuilder publicId = new StringBuilder();
                    for (int i = 7; i < parts.length; i++) {
                        if (i > 7) publicId.append("/");
                        publicId.append(parts[i]);
                    }
                    
                    String result = publicId.toString();
                    // 확장자 제거
                    int lastDot = result.lastIndexOf(".");
                    if (lastDot > 0) {
                        result = result.substring(0, lastDot);
                    }
                    
                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", imageUrl, e);
        }
        
        return null;
    }
    /**
     * 프로필 이미지 업로드 (User용) - 완전히 작동하는 버전
     */
    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        log.info("프로필 이미지 업로드 시작 - 사용자: {}, 파일명: {}, 크기: {}bytes", 
                userId, file.getOriginalFilename(), file.getSize());
        
        try {
            // 파일 유효성 검사
            validateImageFile(file);
            
            // 업로드 파라미터 - transformation 완전히 제거
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", "checkmate/profiles",
                "public_id", "user_" + userId,
                "overwrite", true,
                "resource_type", "image"
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            
            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("프로필 이미지 업로드 완료 - 사용자: {}, URL: {}", userId, imageUrl);
            
            return imageUrl;
            
        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패 - 사용자: {}, 오류: {}", userId, e.getMessage(), e);
            throw new IOException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 프로필 이미지 삭제
     */
    public boolean deleteProfileImage(Long userId) {
        String publicId = "checkmate/profiles/user_" + userId;
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String deleteResult = (String) result.get("result");
            
            boolean isSuccess = "ok".equals(deleteResult);
            log.info("프로필 이미지 삭제 {} - 사용자: {}, 결과: {}", 
                    isSuccess ? "성공" : "실패", userId, deleteResult);
            
            return isSuccess;
            
        } catch (IOException e) {
            log.error("프로필 이미지 삭제 실패 - 사용자: {}, 오류: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 썸네일 URL 생성 - 원본 URL만 반환
     */
    public String generateThumbnailUrl(Long userId, int width, int height) {
        String publicId = "checkmate/profiles/user_" + userId;
        
        // 일단 원본 URL만 반환 (나중에 클라이언트에서 변환 가능)
        String originalUrl = cloudinary.url().generate(publicId);
        
        log.debug("썸네일 URL 생성 - 사용자: {}, 크기: {}x{}, URL: {}", userId, width, height, originalUrl);
        
        return originalUrl;
    }
}