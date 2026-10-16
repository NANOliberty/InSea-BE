package checkmate.insea.review.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import checkmate.insea.map.model.Place;
import checkmate.insea.map.repository.PlaceRepository;
import checkmate.insea.review.model.Review;
import checkmate.insea.review.model.ReviewLike;
import checkmate.insea.review.repository.ReviewLikeRepository;
import checkmate.insea.review.repository.ReviewRepository;
import checkmate.insea.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final PlaceRepository placeRepository;
    
    /**
     * 리뷰 작성
     */
    // public Review createReview(Long placeId, User user, Integer rating, String title, String content, List<String> imageUrls) {
    //     // 수정: ACTIVE 상태 리뷰만 체크하도록 변경
    //     if (reviewRepository.existsByPlaceIdAndUserIdAndStatus(placeId, user.getUserId(), Review.ReviewStatus.ACTIVE)) {
    //         throw new IllegalStateException("이미 이 장소에 리뷰를 작성하셨습니다.");
    //     }
        
    //     Place place = placeRepository.findById(placeId)
    //             .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다."));
        
    //     // 평점 유효성 검사
    //     if (rating < 1 || rating > 5) {
    //         throw new IllegalArgumentException("평점은 1-5 사이여야 합니다.");
    //     }
        
    //     Review review = Review.builder()
    //             .place(place)
    //             .user(user)
    //             .authorName(user.getNickname())
    //             .rating(rating)
    //             .title(title)
    //             .content(content)
    //             .imageUrls(imageUrls != null ? imageUrls : List.of())
    //             .build();
        
    //     Review savedReview = reviewRepository.save(review);
        
    //     // Place의 평점 통계 업데이트
    //     place.addReview(rating);
    //     placeRepository.save(place);
        
    //     return savedReview;
    // }
    
    /**
     * 리뷰 작성 - 삭제된 리뷰 재활용 방식
    */
    public Review createReview(Long placeId, User user, Integer rating, String title, String content, List<String> imageUrls) {
        // 평점 유효성 검사 (재활용·신규 공통)
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1-5 사이여야 합니다.");
        }

        // 1. 기존 삭제된 리뷰가 있는지 확인
        Optional<Review> existingDeletedReview = reviewRepository.findByPlaceIdAndUserIdAndStatus(
            placeId, user.getUserId(), Review.ReviewStatus.DELETED);
        
        if (existingDeletedReview.isPresent()) {
            // 삭제된 리뷰를 재활용
            Review review = existingDeletedReview.get();
            
            
            // 리뷰 정보 업데이트
            review.setStatus(Review.ReviewStatus.ACTIVE);
            review.setRating(rating);
            review.setTitle(title);
            review.setContent(content);
            review.setImageUrls(imageUrls != null ? imageUrls : List.of());
            review.setAuthorName(user.getNickname());
            review.setLikeCount(0); // 좋아요 수 초기화
            review.setReportCount(0); // 신고 수 초기화
            review.setUpdatedAt(LocalDateTime.now());
            
            Review savedReview = reviewRepository.save(review);
            
            // Place 통계 업데이트 (기존 삭제된 평점 제거하고 새 평점 추가)
            Place place = review.getPlace();
            // 기존 평점은 deleteReview() 시점에 이미 removeReview()로 빠져 있으므로 새 평점만 추가한다
            place.addReview(rating);
            placeRepository.save(place);
            
            return savedReview;
        }
        
        // 2. 활성 리뷰가 이미 있는지 확인
        if (reviewRepository.existsByPlaceIdAndUserIdAndStatus(placeId, user.getUserId(), Review.ReviewStatus.ACTIVE)) {
            throw new IllegalStateException("이미 이 장소에 리뷰를 작성하셨습니다.");
        }
        
        // 3. 새 리뷰 생성 (기존 로직)
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다."));
        
        
        Review review = Review.builder()
                .place(place)
                .user(user)
                .authorName(user.getNickname())
                .rating(rating)
                .title(title)
                .content(content)
                .imageUrls(imageUrls != null ? imageUrls : List.of())
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        // Place의 평점 통계 업데이트
        place.addReview(rating);
        placeRepository.save(place);
        
        return savedReview;
    }
        
    /**
     * 리뷰 수정
     */
    public Review updateReview(Long reviewId, User user, Integer rating, String title, String content, List<String> imageUrls) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
        
        // 리뷰 작성자인지 확인
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("리뷰 작성자만 수정할 수 있습니다.");
        }

        // 삭제된 리뷰는 수정 불가 (수정하면 Place 통계가 어긋난다)
        if (review.getStatus() != Review.ReviewStatus.ACTIVE) {
            throw new IllegalStateException("삭제된 리뷰는 수정할 수 없습니다.");
        }
        
        // 평점 유효성 검사
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1-5 사이여야 합니다.");
        }
        
        Integer oldRating = review.getRating();
        
        review.setRating(rating);
        review.setTitle(title);
        review.setContent(content);
        review.setImageUrls(imageUrls != null ? imageUrls : List.of());
        review.setAuthorName(user.getNickname()); // author_name도 업데이트
        
        Review savedReview = reviewRepository.save(review);
        
        // Place의 평점 통계 업데이트 (기존 평점 제거 후 새 평점 추가)
        Place place = review.getPlace();
        place.updateReview(oldRating, rating);
        placeRepository.save(place);
        
        return savedReview;
    }
    
    /**
     * 리뷰 삭제 (소프트 삭제)
     */
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
        
        // 리뷰 작성자인지 확인
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("리뷰 작성자만 삭제할 수 있습니다.");
        }

        // 이미 삭제된 리뷰를 다시 삭제하면 Place 통계가 반복 차감되므로 막는다
        if (review.getStatus() != Review.ReviewStatus.ACTIVE) {
            throw new IllegalStateException("이미 삭제된 리뷰입니다.");
        }
        
        // 소프트 삭제
        review.setStatus(Review.ReviewStatus.DELETED);
        reviewRepository.save(review);
        
        // Place의 평점 통계 업데이트
        Place place = review.getPlace();
        place.removeReview(review.getRating());
        placeRepository.save(place);
    }
    
    /**
     * 리뷰 좋아요 토글
     */
    public boolean toggleReviewLike(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
        
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId, user.getUserId());
        
        if (existingLike.isPresent()) {
            // 좋아요 해제
            reviewLikeRepository.delete(existingLike.get());
            review.setLikeCount(Math.max(0, review.getLikeCount() - 1));
            reviewRepository.save(review);
            return false;
        } else {
            // 좋아요 추가
            ReviewLike reviewLike = ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .build();
            reviewLikeRepository.save(reviewLike);
            review.setLikeCount(review.getLikeCount() + 1);
            reviewRepository.save(review);
            return true;
        }
    }
    
    /**
     * 장소별 리뷰 목록 조회 (활성 상태만)
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsByPlace(Long placeId) {
        return reviewRepository.findByPlaceIdAndStatusOrderByCreatedAtDesc(placeId, Review.ReviewStatus.ACTIVE);
    }
    
    /**
     * 사용자별 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserIdAndStatus(userId, Review.ReviewStatus.ACTIVE);
    }
    
    /**
     * 사용자가 좋아요를 누른 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Review> getLikedReviewsByUser(Long userId) {
        return reviewRepository.findLikedReviewsByUser(userId, Review.ReviewStatus.ACTIVE);
    }
    
    /**
     * 사용자가 특정 장소에 리뷰를 작성했는지 확인
     */
    @Transactional(readOnly = true)
    public boolean hasUserReviewedPlace(Long placeId, Long userId) {
        return reviewRepository.existsByPlaceIdAndUserIdAndStatus(placeId, userId, Review.ReviewStatus.ACTIVE);
    }
}