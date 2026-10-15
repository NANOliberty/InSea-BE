package checkmate.insea.fish_info.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 금어기 통계 정보를 담는 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FishingBanStatistics {
    private int year;
    private int totalBanPeriods;
    private long fullBanCount;
    private long sizeRestrictionCount;
    private long uniqueSpeciesCount;
}