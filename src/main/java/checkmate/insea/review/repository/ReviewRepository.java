package checkmate.insea.review.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import checkmate.insea.review.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // 장소별 리뷰 조회 (활성 상태만, 최신순)
    List<Review> findByPlaceIdAndStatusOrderByCreatedAtDesc(Long placeId, Review.ReviewStatus status);
    
    // 장소별 리뷰 조회 (페이징)
    Page<Review> findByPlaceIdAndStatusOrderByCreatedAtDesc(Long placeId, Review.ReviewStatus status, Pageable pageable);
    
    // 사용자별 리뷰 조회 - 직접 쿼리로 작성
    @Query("SELECT r FROM Review r WHERE r.user.userId = :userId AND r.status = :status ORDER BY r.createdAt DESC")
    List<Review> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Review.ReviewStatus status);
    
    // 사용자가 특정 장소에 리뷰를 작성했는지 확인 - 직접 쿼리로 작성
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.place.id = :placeId AND r.user.userId = :userId")
    boolean existsByPlaceIdAndUserId(@Param("placeId") Long placeId, @Param("userId") Long userId);
    
    // 사용자가 특정 장소에 활성 리뷰를 작성했는지 확인 - 직접 쿼리로 작성
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.place.id = :placeId AND r.user.userId = :userId AND r.status = :status")
    boolean existsByPlaceIdAndUserIdAndStatus(@Param("placeId") Long placeId, @Param("userId") Long userId, @Param("status") Review.ReviewStatus status);
    
    // 장소별 평점 통계 (실시간 계산용 - 캐시된 값과 검증시 사용)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.place.id = :placeId AND r.status = :status")
    Double calculateAverageRatingByPlaceId(@Param("placeId") Long placeId, @Param("status") Review.ReviewStatus status);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.place.id = :placeId AND r.status = :status")
    Long countByPlaceIdAndStatus(@Param("placeId") Long placeId, @Param("status") Review.ReviewStatus status);
    
    // 평점별 개수 조회
    @Query("SELECT COUNT(r) FROM Review r WHERE r.place.id = :placeId AND r.rating = :rating AND r.status = :status")
    Long countByPlaceIdAndRatingAndStatus(@Param("placeId") Long placeId, @Param("rating") Integer rating, @Param("status") Review.ReviewStatus status);
    
    // 인기 리뷰 조회 (좋아요 수 기준)
    @Query("SELECT r FROM Review r WHERE r.place.id = :placeId AND r.status = :status ORDER BY r.likeCount DESC, r.createdAt DESC")
    List<Review> findPopularReviewsByPlaceId(@Param("placeId") Long placeId, @Param("status") Review.ReviewStatus status, Pageable pageable);
    
    // 높은 평점 리뷰 조회
    @Query("SELECT r FROM Review r WHERE r.place.id = :placeId AND r.rating >= :minRating AND r.status = :status ORDER BY r.rating DESC, r.createdAt DESC")
    List<Review> findHighRatedReviewsByPlaceId(@Param("placeId") Long placeId, @Param("minRating") Integer minRating, @Param("status") Review.ReviewStatus status, Pageable pageable);
    
    // 최근 리뷰 조회 (특정 기간)
    @Query("SELECT r FROM Review r WHERE r.place.id = :placeId AND r.status = :status AND r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<Review> findRecentReviewsByPlaceId(@Param("placeId") Long placeId, @Param("status") Review.ReviewStatus status, @Param("since") LocalDateTime since);
    
    // 사용자가 좋아요를 누른 리뷰 목록 조회
    @Query("SELECT r FROM Review r INNER JOIN ReviewLike rl ON r.id = rl.review.id WHERE rl.user.userId = :userId AND r.status = :status ORDER BY rl.createdAt DESC")
    List<Review> findLikedReviewsByUser(@Param("userId") Long userId, @Param("status") Review.ReviewStatus status);
    
    // 사용자의 특정 장소 리뷰 조회 - 직접 쿼리로 작성
    @Query("SELECT r FROM Review r WHERE r.place.id = :placeId AND r.user.userId = :userId AND r.status = :status")
    Optional<Review> findByPlaceIdAndUserIdAndStatus(@Param("placeId") Long placeId, @Param("userId") Long userId, @Param("status") Review.ReviewStatus status);
}