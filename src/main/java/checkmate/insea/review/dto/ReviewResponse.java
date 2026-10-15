// ReviewResponse.java
package checkmate.insea.review.dto;

import java.time.LocalDateTime;
import java.util.List;

import checkmate.insea.review.model.Review;
import checkmate.insea.user.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponse {
    
    private final Long reviewId;
    private final Long placeId;
    private final String placeName;
    private final Long userId;
    private final String userNickname;
    private final String userProfileImage;
    private final Integer rating;
    private final String title;
    private final String content;
    private final List<String> imageUrls;
    private final Integer likeCount;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId()) // getId() 메서드 사용
                .placeId(review.getPlace().getId())
                .placeName(review.getPlace().getName())
                .userId(review.getUser().getUserId())
                .userNickname(review.getAuthorName() != null ? review.getAuthorName() : review.getUser().getNickname())
                .userProfileImage(review.getUser().getProfileImageUrl())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .imageUrls(review.getImageUrls())
                .likeCount(review.getLikeCount())
                .status(review.getStatus().name())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
        // ReviewResponse.java에 추가
    public static ReviewResponse fromWithUser(Review review, User user) {
        return ReviewResponse.builder()
            .reviewId(review.getReviewId())
            .placeId(review.getPlace().getId())
            .placeName(review.getPlace().getName())
            .userId(user.getUserId())  // 파라미터 user 사용
            .userNickname(user.getNickname())  // 파라미터 user 사용
            .userProfileImage(user.getProfileImageUrl())  // 파라미터 user 사용
            .rating(review.getRating())
            .title(review.getTitle())
            .content(review.getContent())
            .imageUrls(review.getImageUrls())
            .likeCount(review.getLikeCount())
            .status(review.getStatus().name())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .build();
    }

    public static ReviewResponse fromBasic(Review review) {
        return ReviewResponse.builder()
            .reviewId(review.getReviewId())
            .placeId(review.getPlace().getId())
            .placeName(review.getPlace().getName())
            .userId(null)
            .userNickname(review.getAuthorName())  // 캐시된 값 사용
            .userProfileImage(null)  // 기본값
            .rating(review.getRating())
            .title(review.getTitle())
            .content(review.getContent())
            .imageUrls(review.getImageUrls())
            .likeCount(review.getLikeCount())
            .status(review.getStatus().name())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .build();
    }
}