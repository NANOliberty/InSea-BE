package checkmate.insea.review.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import checkmate.insea.map.model.Place;
import checkmate.insea.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reviews") // 사용자당 장소별 1개 리뷰만
@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long reviewId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName; // 작성자 이름 캐시 (성능 최적화용)
    
    @Column(nullable = false)
    private Integer rating; // 1-5
    
    @Column(length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    // 리뷰 이미지들을 JSON 배열로 저장 (URL 리스트)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
    
    // 좋아요 수 캐시
    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;
    
    // 신고 수 (관리용)
    @Column(name = "report_count")
    @Builder.Default
    private Integer reportCount = 0;
    
    // 리뷰 상태 (ACTIVE, DELETED, BLOCKED)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.ACTIVE;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum ReviewStatus {
        ACTIVE, DELETED, BLOCKED
    }

        public Long getId() {
        return reviewId;  // DB의 id 컬럼에 접근할 때 사용
    }
    
    public void setId(Long id) {
        this.reviewId = id;
    }
    
    public Long getReviewId() {
        return reviewId;  // 기존 코드 호환성을 위해
    }
}