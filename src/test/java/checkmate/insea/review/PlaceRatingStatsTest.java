package checkmate.insea.review;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import checkmate.insea.map.model.Place;

/**
 * Place 에 비정규화된 평점 통계(총 리뷰 수·점수별 개수·평균)의 증감 규칙 검증.
 * DB 없이 엔티티 메서드만 호출한다.
 */
class PlaceRatingStatsTest {

    private static Place emptyPlace() {
        Place p = new Place() {};   // 추상 클래스이므로 익명 서브클래스
        p.prePersist();            // 통계 필드 0 초기화
        return p;
    }

    @Test
    void addReview_두_건이면_평균은_산술평균() {
        Place p = emptyPlace();
        p.addReview(5);
        p.addReview(3);
        assertThat(p.getTotalReviews()).isEqualTo(2);
        assertThat(p.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.0"));
    }

    @Test
    void removeReview_한_건_빼면_남은_것으로_재계산() {
        Place p = emptyPlace();
        p.addReview(5);
        p.addReview(3);
        p.removeReview(5);
        assertThat(p.getTotalReviews()).isEqualTo(1);
        assertThat(p.getRating5Count()).isZero();
        assertThat(p.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.0"));
    }

    @Test
    void removeReview_마지막_한_건_빼면_0건_평균0() {
        Place p = emptyPlace();
        p.addReview(4);
        p.removeReview(4);
        assertThat(p.getTotalReviews()).isZero();
        assertThat(p.getAverageRating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void updateReview_총_건수는_그대로_평균만_바뀜() {
        Place p = emptyPlace();
        p.addReview(5);
        p.addReview(3);
        p.updateReview(5, 1);
        assertThat(p.getTotalReviews()).isEqualTo(2);
        assertThat(p.getAverageRating()).isEqualByComparingTo(new BigDecimal("2.0"));
    }
}
