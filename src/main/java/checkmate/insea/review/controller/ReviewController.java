package checkmate.insea.review.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import checkmate.insea.image.service.CloudinaryService;
import checkmate.insea.review.dto.ReviewCreateRequest;
import checkmate.insea.review.dto.ReviewResponse;
import checkmate.insea.review.dto.ReviewUpdateRequest;
import checkmate.insea.review.model.Review;
import checkmate.insea.review.service.ReviewService;
import checkmate.insea.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final CloudinaryService cloudinaryService;
    private final ReviewService reviewService;
    
    /**
     * 리뷰 작성
     * POST /api/reviews
     */
    @PostMapping
    @Transactional
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal User user) {
        
        Review review = reviewService.createReview(
            request.getPlaceId(),
            user,
            request.getRating(),
            request.getTitle(),
            request.getContent(),
            request.getImageUrls()
        );
        
        return ResponseEntity.ok(ReviewResponse.fromWithUser(review, user));
    }
    
    /**
     * 리뷰 수정
     * PUT /api/reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    @Transactional
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal User user) {
        
        Review review = reviewService.updateReview(
            reviewId,
            user,
            request.getRating(),
            request.getTitle(),
            request.getContent(),
            request.getImageUrls()
        );
        
        return ResponseEntity.ok(ReviewResponse.fromWithUser(review, user));
    }
    
    /**
     * 리뷰 삭제
     * DELETE /api/reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    @Transactional
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {
        
        reviewService.deleteReview(reviewId, user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "리뷰가 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 장소별 리뷰 목록 조회
     * GET /api/reviews/place/{placeId}
     */
    @GetMapping("/place/{placeId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ReviewResponse>> getReviewsByPlace(@PathVariable Long placeId) {
        
        List<Review> reviews = reviewService.getReviewsByPlace(placeId);
        List<ReviewResponse> response = reviews.stream()
                .map(ReviewResponse::fromBasic)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 내 리뷰 목록 조회
     * GET /api/reviews/my
     */
    @GetMapping("/my")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ReviewResponse>> getMyReviews(@AuthenticationPrincipal User user) {
        
        if (user == null) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다. 로그인이 필요합니다.");
        }
        
        List<Review> reviews = reviewService.getReviewsByUser(user.getUserId());
        List<ReviewResponse> response = reviews.stream()
                .map(review -> ReviewResponse.fromWithUser(review, user))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 리뷰 좋아요 토글
     * POST /api/reviews/{reviewId}/like
     */
    @PostMapping("/{reviewId}/like")
    @Transactional
    public ResponseEntity<Map<String, Object>> toggleReviewLike(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다. 로그인이 필요합니다.");
        }
        
        boolean liked = reviewService.toggleReviewLike(reviewId, user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("message", liked ? "좋아요가 추가되었습니다." : "좋아요가 취소되었습니다.");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 내가 좋아요를 누른 리뷰 목록 조회
     * GET /api/reviews/liked
     */
    @GetMapping("/liked")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ReviewResponse>> getMyLikedReviews(@AuthenticationPrincipal User user) {
        
        if (user == null) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다. 로그인이 필요합니다.");
        }
        
        List<Review> likedReviews = reviewService.getLikedReviewsByUser(user.getUserId());
        List<ReviewResponse> response = likedReviews.stream()
                .map(review -> ReviewResponse.fromWithUser(review, user))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 사용자가 특정 장소에 리뷰를 작성했는지 확인
     * GET /api/reviews/check/{placeId}
     */
    @GetMapping("/check/{placeId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Boolean>> checkUserReviewed(
            @PathVariable Long placeId,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다. 로그인이 필요합니다.");
        }
        
        boolean hasReviewed = reviewService.hasUserReviewedPlace(placeId, user.getUserId());
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasReviewed", hasReviewed);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 이미지 업로드
     * POST /api/reviews/images
     */
    @PostMapping("/images")
    @Transactional
    public ResponseEntity<Map<String, Object>> uploadImages(
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다. 로그인이 필요합니다.");
        }
        
        try {
            List<String> imageUrls = cloudinaryService.uploadImages(images, "reviews");
            
            Map<String, Object> response = new HashMap<>();
            response.put("imageUrls", imageUrls);
            response.put("message", "이미지 업로드가 완료되었습니다.");
            response.put("count", imageUrls.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "이미지 업로드에 실패했습니다.");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}