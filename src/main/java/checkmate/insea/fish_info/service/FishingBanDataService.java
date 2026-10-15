package checkmate.insea.fish_info.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import checkmate.insea.fish_info.config.BanPeriodData;
import checkmate.insea.fish_info.config.FishingBanConfig;
import checkmate.insea.fish_info.config.FishingBanStatistics;
import checkmate.insea.fish_info.mapper.FishSpeciesRepository;
import checkmate.insea.fish_info.mapper.FishingBanPeriodRepository;
import checkmate.insea.fish_info.model.EnumClass.BanType;
import checkmate.insea.fish_info.model.EnumClass.FishRarity;
import checkmate.insea.fish_info.model.FishSpecies;
import checkmate.insea.fish_info.model.FishingBanPeriod;

@Service
@Transactional
public class FishingBanDataService {
    
    private final FishingBanPeriodRepository fishingBanPeriodRepository;  
    private final FishSpeciesRepository fishSpeciesRepository;
    private final FishingBanConfig fishingBanConfig;
    
    public FishingBanDataService(FishingBanPeriodRepository fishingBanPeriodRepository,  
                                FishSpeciesRepository fishSpeciesRepository,
                                FishingBanConfig fishingBanConfig) {  
        this.fishingBanPeriodRepository = fishingBanPeriodRepository;  
        this.fishSpeciesRepository = fishSpeciesRepository;
        this.fishingBanConfig = fishingBanConfig;
    } 

    public List<FishingBanPeriod> findCurrentBanPeriods(int year, LocalDate currentDate) {
        return fishingBanPeriodRepository.findCurrentBanPeriods(year, currentDate);
    }
    
    /**
     * 설정 파일에서 금어기 정보 일괄 저장
     */
    public void saveFishingBanPeriodsFromConfig() {
        int year = fishingBanConfig.getYear();
        System.out.println(year + "년 금어기 정보 저장 시작 (설정 파일 기반)");
        
        // 기존 해당 연도 데이터 삭제 (간단한 방법 사용)
        List<FishingBanPeriod> existingPeriods = fishingBanPeriodRepository.findByYear(year);
        if (!existingPeriods.isEmpty()) {
            fishingBanPeriodRepository.deleteAll(existingPeriods);
            System.out.println("기존 " + year + "년 금어기 데이터 " + existingPeriods.size() + "건 삭제");
        }
        
        // 설정 파일의 모든 금어기 데이터 처리
        List<BanPeriodData> allBanPeriods = fishingBanConfig.getAllBanPeriods();
        int successCount = 0;
        int failCount = 0;
        
        for (BanPeriodData banData : allBanPeriods) {
            try {
                saveBanPeriodFromConfig(banData, year);
                successCount++;
            } catch (Exception e) {
                failCount++;
                System.err.println("금어기 정보 저장 실패: " + banData.getKoreanName() + " - " + e.getMessage());
            }
        }
        
        System.out.println(year + "년 금어기 정보 저장 완료: 성공 " + successCount + "건, 실패 " + failCount + "건");
    }
    
    /**
     * 특정 금어기 정보를 설정 데이터에서 저장
     */
    private void saveBanPeriodFromConfig(BanPeriodData banData, int year) {
        // 어종 찾기 또는 생성
        Long fishId = findOrCreateFishSpecies(banData.getKoreanName(), banData.getScientificName());
        
        // 날짜 변환
        LocalDate startDate = banData.getStartDateAsLocalDate(year);
        LocalDate endDate = banData.getEndDateAsLocalDate(year);
        
        // 금어기 정보 저장 - Lombok 빌더 패턴 제거하고 setter 사용
        FishingBanPeriod banPeriod = new FishingBanPeriod();
        banPeriod.setFishId(fishId);
        banPeriod.setStartDate(startDate);
        banPeriod.setEndDate(endDate);
        banPeriod.setRegion(banData.getRegion());
        banPeriod.setBanType(banData.getBanType());
        banPeriod.setMinSize(banData.getMinSize());
        banPeriod.setDescription(banData.getDescription());
        banPeriod.setYear(year);
        
        fishingBanPeriodRepository.save(banPeriod);
        
        System.out.println("금어기 정보 저장: " + banData.getKoreanName() + " (" + banData.getRegion() + ") - " + startDate + " ~ " + endDate);
    }
    
    /**
     * 어종 찾기 또는 새로 생성
     */
    private Long findOrCreateFishSpecies(String koreanName, String scientificName) {
        Optional<FishSpecies> fishOpt = fishSpeciesRepository
            .findByKoreanNameOrScientificName(koreanName, scientificName);
        
        if (fishOpt.isPresent()) {
            return fishOpt.get().getFishId();
        }
        
        // 어종이 없으면 새로 생성
        System.out.println("어종을 찾을 수 없어 새로 생성: " + koreanName + " (" + scientificName + ")");
        
        try {
            // Lombok 빌더 패턴 제거하고 setter 사용
            FishSpecies newFish = new FishSpecies();
            newFish.setKoreanName(koreanName);
            newFish.setScientificName(scientificName);
            newFish.setRarity(FishRarity.COMMON);
            
            FishSpecies savedFish = fishSpeciesRepository.save(newFish);
            return savedFish.getFishId();
        } catch (DataIntegrityViolationException e) {
            // 동시성 문제로 중복 생성된 경우 다시 조회
            fishOpt = fishSpeciesRepository.findByKoreanNameOrScientificName(koreanName, scientificName);
            return fishOpt.orElseThrow(() -> 
                new RuntimeException("어종 생성 및 조회 실패: " + koreanName)).getFishId();
        }
    }
    
    /**
     * 설정 파일 정보 조회
     */
    @Transactional(readOnly = true)
    public FishingBanConfig getFishingBanConfig() {
        return fishingBanConfig;
    }
    
    /**
     * 특정 연도의 금어기 통계 조회
     */
    @Transactional(readOnly = true)
    public FishingBanStatistics getFishingBanStatistics(int year) {
        List<FishingBanPeriod> allPeriods = fishingBanPeriodRepository.findByYear(year);
        
        long fullBanCount = allPeriods.stream()
            .filter(period -> period.getBanType() == BanType.FULL_BAN)
            .count();
        
        long sizeRestrictionCount = allPeriods.stream()
            .filter(period -> period.getBanType() == BanType.SIZE_RESTRICTION)
            .count();
        
        long uniqueSpeciesCount = allPeriods.stream()
            .map(FishingBanPeriod::getFishId)
            .distinct()
            .count();
        
        // Lombok 빌더 패턴 제거하고 setter 사용
        FishingBanStatistics stats = new FishingBanStatistics();
        stats.setYear(year);
        stats.setTotalBanPeriods(allPeriods.size());
        stats.setFullBanCount(fullBanCount);
        stats.setSizeRestrictionCount(sizeRestrictionCount);
        stats.setUniqueSpeciesCount(uniqueSpeciesCount);
        
        return stats;
    }
    
    // ===== 기존 하드코딩된 메서드들 (Deprecated) =====
    
    /**
     * @deprecated 설정 파일 기반 방식으로 변경됨. saveFishingBanPeriodsFromConfig() 사용 권장
     */
    @Deprecated
    public void saveFishingBanPeriods2025() {
        System.out.println("하드코딩된 메서드는 deprecated 되었습니다. saveFishingBanPeriodsFromConfig()를 사용하세요.");
        saveFishingBanPeriodsFromConfig();
    }
}