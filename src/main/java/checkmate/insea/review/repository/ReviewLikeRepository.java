package checkmate.insea.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import checkmate.insea.review.model.ReviewLike;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    
    // 특정 리뷰에 특정 사용자의 좋아요 조회 - 직접 쿼리
    @Query("SELECT rl FROM ReviewLike rl WHERE rl.review.reviewId = :reviewId AND rl.user.userId = :userId")
    Optional<ReviewLike> findByReviewIdAndUserId(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
    
    // 특정 리뷰의 좋아요 목록
    @Query("SELECT rl FROM ReviewLike rl WHERE rl.review.reviewId = :reviewId")
    List<ReviewLike> findByReviewId(@Param("reviewId") Long reviewId);
    
    // 특정 사용자가 좋아요한 리뷰 목록
    @Query("SELECT rl FROM ReviewLike rl WHERE rl.user.userId = :userId")
    List<ReviewLike> findByUserId(@Param("userId") Long userId);
    
    // 특정 리뷰의 좋아요 수
    @Query("SELECT COUNT(rl) FROM ReviewLike rl WHERE rl.review.reviewId = :reviewId")
    Long countByReviewId(@Param("reviewId") Long reviewId);
    
    // 사용자가 리뷰에 좋아요를 눌렀는지 확인
    @Query("SELECT COUNT(rl) > 0 FROM ReviewLike rl WHERE rl.review.reviewId = :reviewId AND rl.user.userId = :userId")
    boolean existsByReviewIdAndUserId(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
}