package checkmate.insea.fish_info.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import checkmate.insea.fish_info.config.BanPeriodData;
import checkmate.insea.fish_info.config.FishingBanConfig;
import checkmate.insea.fish_info.service.FishingBanDataService;
import lombok.extern.slf4j.Slf4j;

/**
 * 실용적인 금어기 조회 API
 */
@RestController
@RequestMapping("/api/fishing")
@Slf4j
public class FishingBanQueryController {
    
    private final FishingBanDataService fishingBanDataService;
    
    public FishingBanQueryController(FishingBanDataService fishingBanDataService) {
        this.fishingBanDataService = fishingBanDataService;
    }
    
    /**
     * 특정 날짜에 낚시 가능한 어종 조회
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableFish(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String region) {
        
        try {
            FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
            List<BanPeriodData> allPeriods = config.getAllBanPeriods();
            
            // 지정된 날짜에 금어기가 아닌 어종들 찾기
            List<String> availableFish = allPeriods.stream()
                .filter(period -> region == null || period.getRegion().contains(region) || period.getRegion().equals("전국"))
                .filter(period -> !isDateInBanPeriod(date, period, config.getYear()))
                .map(BanPeriodData::getKoreanName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("date", date);
            response.put("region", region != null ? region : "전국");
            response.put("availableFish", availableFish);
            response.put("count", availableFish.size());
            response.put("message", String.format("%s에 %s에서 낚시 가능한 어종", 
                date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")), 
                region != null ? region : "전국"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("낚시 가능 어종 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 특정 날짜에 금어기인 어종 조회
     */
    @GetMapping("/banned")
    public ResponseEntity<Map<String, Object>> getBannedFish(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String region) {
        
        try {
            FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
            List<BanPeriodData> allPeriods = config.getAllBanPeriods();
            
            // 지정된 날짜에 금어기인 어종들 찾기
            List<Map<String, Object>> bannedFish = allPeriods.stream()
                .filter(period -> region == null || period.getRegion().contains(region) || period.getRegion().equals("전국"))
                .filter(period -> isDateInBanPeriod(date, period, config.getYear()))
                .map(period -> {
                    Map<String, Object> fishInfo = new HashMap<>();
                    fishInfo.put("name", period.getKoreanName());
                    fishInfo.put("scientificName", period.getScientificName());
                    fishInfo.put("region", period.getRegion());
                    fishInfo.put("banType", period.getBanType());
                    fishInfo.put("minSize", period.getMinSize());
                    fishInfo.put("reason", period.getDescription());
                    fishInfo.put("startDate", period.getStartDate());
                    fishInfo.put("endDate", period.getEndDate());
                    return fishInfo;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("date", date);
            response.put("region", region != null ? region : "전국");
            response.put("bannedFish", bannedFish);
            response.put("count", bannedFish.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("금어기 어종 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 특정 어종의 연간 금어기 스케줄 조회
     */
    @GetMapping("/schedule/{fishName}")
    public ResponseEntity<Map<String, Object>> getFishSchedule(@PathVariable String fishName) {
        
        try {
            FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
            List<BanPeriodData> fishPeriods = config.getAllBanPeriods().stream()
                .filter(period -> period.getKoreanName().contains(fishName))
                .collect(Collectors.toList());
            
            if (fishPeriods.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "fishName", fishName,
                    "message", "해당 어종의 금어기 정보를 찾을 수 없습니다",
                    "schedule", Collections.emptyList()
                ));
            }
            
            List<Map<String, Object>> schedule = fishPeriods.stream()
                .map(period -> {
                    Map<String, Object> scheduleItem = new HashMap<>();
                    scheduleItem.put("region", period.getRegion());
                    scheduleItem.put("startDate", period.getStartDate());
                    scheduleItem.put("endDate", period.getEndDate());
                    scheduleItem.put("banType", period.getBanType());
                    scheduleItem.put("minSize", period.getMinSize());
                    scheduleItem.put("description", period.getDescription());
                    scheduleItem.put("crossYear", period.isCrossYear());
                    return scheduleItem;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("fishName", fishName);
            response.put("scientificName", fishPeriods.get(0).getScientificName());
            response.put("year", config.getYear());
            response.put("schedule", schedule);
            response.put("totalPeriods", schedule.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("어종 스케줄 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 월별 금어기 통계
     */
    @GetMapping("/monthly-stats")
    public ResponseEntity<Map<String, Object>> getMonthlyStats() {
        
        try {
            FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
            List<BanPeriodData> allPeriods = config.getAllBanPeriods();
            
            Map<Integer, Long> monthlyBanCount = new HashMap<>();
            Map<Integer, List<String>> monthlyBannedFish = new HashMap<>();
            
            for (int month = 1; month <= 12; month++) {
                LocalDate testDate = LocalDate.of(config.getYear(), month, 15); // 각 월 중순으로 테스트
                
                List<String> bannedInMonth = allPeriods.stream()
                    .filter(period -> isDateInBanPeriod(testDate, period, config.getYear()))
                    .map(BanPeriodData::getKoreanName)
                    .distinct()
                    .collect(Collectors.toList());
                
                monthlyBanCount.put(month, (long) bannedInMonth.size());
                monthlyBannedFish.put(month, bannedInMonth);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("year", config.getYear());
            response.put("monthlyBanCount", monthlyBanCount);
            response.put("monthlyBannedFish", monthlyBannedFish);
            response.put("peakBanMonth", monthlyBanCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("월별 통계 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 지역별 금어기 정보
     */
    @GetMapping("/regions")
    public ResponseEntity<Map<String, Object>> getRegionInfo() {
        
        try {
            FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
            List<BanPeriodData> allPeriods = config.getAllBanPeriods();
            
            Map<String, List<String>> regionFish = allPeriods.stream()
                .collect(Collectors.groupingBy(
                    BanPeriodData::getRegion,
                    Collectors.mapping(BanPeriodData::getKoreanName, 
                        Collectors.toList())
                ));
            
            Map<String, Object> regionStats = regionFish.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> Map.of(
                        "fishCount", entry.getValue().size(),
                        "uniqueFishCount", entry.getValue().stream().distinct().count(),
                        "fishList", entry.getValue().stream().distinct().sorted().collect(Collectors.toList())
                    )
                ));
            
            Map<String, Object> response = new HashMap<>();
            response.put("regions", regionStats);
            response.put("totalRegions", regionStats.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("지역별 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 날짜가 금어기에 해당하는지 확인하는 헬퍼 메서드
     */
    private boolean isDateInBanPeriod(LocalDate date, BanPeriodData period, int year) {
        try {
            String[] startParts = period.getStartDate().split("-");
            String[] endParts = period.getEndDate().split("-");
            
            LocalDate startDate = LocalDate.of(year, Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]));
            LocalDate endDate = LocalDate.of(year, Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));
            
            // 연도를 넘나드는 경우 처리
            if (period.isCrossYear()) {
                endDate = endDate.plusYears(1);
                if (date.isBefore(startDate)) {
                    date = date.plusYears(1);
                }
            }
            
            return !date.isBefore(startDate) && !date.isAfter(endDate);
            
        } catch (NumberFormatException e) {
            log.warn("날짜 파싱 실패: {}", e.getMessage());
            return false;
        }
    }
}