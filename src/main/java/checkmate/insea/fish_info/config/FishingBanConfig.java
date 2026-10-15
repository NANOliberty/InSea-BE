package checkmate.insea.fish_info.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 금어기 설정 데이터를 담는 메인 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FishingBanConfig {
    private int year;
    private LocalDateTime lastUpdated;
    
    @Builder.Default
    private List<BanPeriodData> fishBanPeriods = new ArrayList<>();
    
    @Builder.Default
    private List<BanPeriodData> crustaceanBanPeriods = new ArrayList<>();
    
    @Builder.Default
    private List<BanPeriodData> molluskBanPeriods = new ArrayList<>();
    
    @Builder.Default
    private List<BanPeriodData> seaweedBanPeriods = new ArrayList<>();
    
    @Builder.Default
    private List<BanPeriodData> otherMarineBanPeriods = new ArrayList<>();
    
    /**
     * 모든 금어기 데이터를 하나의 리스트로 반환
     */
    public List<BanPeriodData> getAllBanPeriods() {
        List<BanPeriodData> allPeriods = new ArrayList<>();
        allPeriods.addAll(fishBanPeriods);
        allPeriods.addAll(crustaceanBanPeriods);
        allPeriods.addAll(molluskBanPeriods);
        allPeriods.addAll(seaweedBanPeriods);
        allPeriods.addAll(otherMarineBanPeriods);
        return allPeriods;
    }
}