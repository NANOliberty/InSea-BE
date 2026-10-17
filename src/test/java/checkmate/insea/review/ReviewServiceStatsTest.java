package checkmate.insea.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import checkmate.insea.map.model.Place;
import checkmate.insea.map.repository.PlaceRepository;
import checkmate.insea.review.model.Review;
import checkmate.insea.review.repository.ReviewLikeRepository;
import checkmate.insea.review.repository.ReviewRepository;
import checkmate.insea.review.service.ReviewService;
import checkmate.insea.user.model.User;

/**
 * 리뷰 상태 전이(작성 → 삭제 → 재작성)에 따른 Place 평점 통계 정합성.
 * 2026-10 코드 검토에서 발견한 "삭제 후 재작성 시 이중 차감" 회귀 테스트.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // setUp의 save() 스텁은 예외로 끝나는 케이스에선 안 쓰인다
class ReviewServiceStatsTest {

    @Mock ReviewRepository reviewRepository;
    @Mock ReviewLikeRepository reviewLikeRepository;
    @Mock PlaceRepository placeRepository;

    ReviewService service;
    Place place;
    User userA;
    User userB;

    @BeforeEach
    void setUp() {
        service = new ReviewService(reviewRepository, reviewLikeRepository, placeRepository);
        place = new Place() {};
        place.prePersist();
        place.setId(1L);
        userA = User.builder().userId(10L).nickname("A").build();
        userB = User.builder().userId(20L).nickname("B").build();

        // save()는 받은 객체를 그대로 돌려준다
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
        when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Review activeReview(long id, User user, int rating) {
        Review r = Review.builder().place(place).user(user).rating(rating)
                .title("t").content("c").imageUrls(List.of()).build();
        r.setId(id);
        return r;
    }

    @Test
    void 다른_리뷰가_있는_상태에서_삭제_후_재작성하면_통계가_정확하다() {
        // A 5점, B 3점 → 2건, 평균 4.0
        place.addReview(5);
        place.addReview(3);
        Review a = activeReview(100L, userA, 5);

        // A 삭제 → 1건, 평균 3.0
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(a));
        service.deleteReview(100L, userA);
        assertThat(a.getStatus()).isEqualTo(Review.ReviewStatus.DELETED);
        assertThat(place.getTotalReviews()).isEqualTo(1);
        assertThat(place.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.0"));

        // A 4점으로 재작성 → 삭제된 행 재활용. 기대: 2건, 평균 3.5
        when(reviewRepository.findByPlaceIdAndUserIdAndStatus(eq(1L), eq(10L), eq(Review.ReviewStatus.DELETED)))
                .thenReturn(Optional.of(a));
        Review reborn = service.createReview(1L, userA, 4, "t2", "c2", null);

        assertThat(reborn).isSameAs(a);
        assertThat(reborn.getStatus()).isEqualTo(Review.ReviewStatus.ACTIVE);
        assertThat(reborn.getRating()).isEqualTo(4);
        assertThat(place.getTotalReviews()).isEqualTo(2);          // 수정 전: 1
        assertThat(place.getRating5Count()).isZero();
        assertThat(place.getRating4Count()).isEqualTo(1);
        assertThat(place.getRating3Count()).isEqualTo(1);
        assertThat(place.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.5")); // 수정 전: 7.0
    }

    @Test
    void 이미_삭제된_리뷰를_다시_삭제하면_예외이고_통계는_그대로() {
        place.addReview(5);
        place.addReview(3);
        Review a = activeReview(100L, userA, 5);
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(a));

        service.deleteReview(100L, userA);
        assertThatThrownBy(() -> service.deleteReview(100L, userA))
                .isInstanceOf(IllegalStateException.class);

        assertThat(place.getTotalReviews()).isEqualTo(1);
        assertThat(place.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.0"));
    }

    @Test
    void 삭제된_리뷰는_수정할_수_없다() {
        place.addReview(5);
        Review a = activeReview(100L, userA, 5);
        a.setStatus(Review.ReviewStatus.DELETED);
        place.removeReview(5);
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.updateReview(100L, userA, 2, "t", "c", null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(place.getTotalReviews()).isZero();
    }

    @Test
    void 작성자가_아니면_삭제할_수_없다() {
        place.addReview(5);
        Review a = activeReview(100L, userA, 5);
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.deleteReview(100L, userB))
                .isInstanceOf(IllegalStateException.class);
        assertThat(place.getTotalReviews()).isEqualTo(1);
    }
}
