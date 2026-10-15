package checkmate.insea.fish_info.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import checkmate.insea.fish_info.config.BanPeriodData;
import checkmate.insea.fish_info.config.FishingBanConfig;
import checkmate.insea.fish_info.service.FishingBanDataService;
import lombok.extern.slf4j.Slf4j;

/**
 * 검색 및 추천 기능 API
 */
@RestController
@RequestMapping("/api/fishing/search")
@Slf4j
public class FishingSearchController {
    
    private final FishingBanDataService fishingBanDataService;
    
    public FishingSearchController(FishingBanDataService fishingBanDataService) {
        this.fishingBanDataService = fishingBanDataService;
    }
    
    /**
     * 어종명으로 검색 (자동완성)
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<Map<String, Object>> autocomplete(@RequestParam String query) {
        
        try {
            FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
            
            List<Map<String, Object>> suggestions = config.getAllBanPeriods().stream()
                .filter(period -> period.getKoreanName().contains(query) || 
                                period.getScientificName().toLowerCase().contains(query.toLowerCase()))
                .map(period -> Map.of(
                    "koreanName", period.getKoreanName(),
                    "scientificName", period.getScientificName(),
                    "regions", config.getAllBanPeriods().stream()
                        .filter(p -> p.getKoreanName().equals(period.getKoreanName()))
                        .map(BanPeriodData::getRegion)
                        .distinct()
                        .collect(Collectors.toList())
                ))
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("suggestions", suggestions);
            response.put("count", suggestions.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("자동완성 검색 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "검색 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 낚시 추천 시스템 - 특정 조건에 맞는 어종 추천
     */
    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>> recommendFish(@RequestBody Map<String, Object> criteria) {
        
        try {
            LocalDate fromDate = LocalDate.parse((String) criteria.getOrDefault("fromDate", LocalDate.now().toString()));
            LocalDate toDate = LocalDate.parse((String) criteria.getOrDefault("toDate", LocalDate.now().plusDays(7).toString()));
            String region = (String) criteria.get("region");
            Integer minSize = (Integer) criteria.get("minSize");
            
            FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
            
            // 기간 내에 낚시 가능한 어종들 찾기
            Set<String> availableFish = new HashSet<>();
            
            LocalDate currentDate = fromDate;
            while (!currentDate.isAfter(toDate)) {
                final LocalDate checkDate = currentDate;
                
                List<String> dailyAvailable = config.getAllBanPeriods().stream()
                    .filter(period -> region == null || period.getRegion().contains(region) || period.getRegion().equals("전국"))
                    .filter(period -> !isDateInBanPeriod(checkDate, period, config.getYear()))
                    .filter(period -> minSize == null || period.getMinSize() == null || period.getMinSize() < minSize)
                    .map(BanPeriodData::getKoreanName)
                    .collect(Collectors.toList());
                
                availableFish.addAll(dailyAvailable);
                currentDate = currentDate.plusDays(1);
            }
            
            // 어종별 추천 점수 계산
            List<Map<String, Object>> recommendations = availableFish.stream()
                .map(fishName -> {
                    List<BanPeriodData> fishPeriods = config.getAllBanPeriods().stream()
                        .filter(period -> period.getKoreanName().equals(fishName))
                        .collect(Collectors.toList());
                    
                    if (fishPeriods.isEmpty()) return null;
                    
                    BanPeriodData representative = fishPeriods.get(0);
                    int score = calculateRecommendationScore(fishPeriods, fromDate, toDate, config.getYear());
                    
                    Map<String, Object> recommendation = new HashMap<>();
                    recommendation.put("fishName", fishName);
                    recommendation.put("scientificName", representative.getScientificName());
                    recommendation.put("score", score);
                    recommendation.put("availableRegions", fishPeriods.stream()
                        .map(BanPeriodData::getRegion)
                        .distinct()
                        .collect(Collectors.toList()));
                    recommendation.put("sizeRestrictions", fishPeriods.stream()
                        .filter(p -> p.getMinSize() != null)
                        .map(p -> Map.of("region", p.getRegion(), "minSize", p.getMinSize()))
                        .collect(Collectors.toList()));
                    
                    return recommendation;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Integer.compare((Integer) b.get("score"), (Integer) a.get("score")))
                .limit(10)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("criteria", criteria);
            response.put("period", fromDate + " ~ " + toDate);
            response.put("recommendations", recommendations);
            response.put("totalFound", recommendations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("낚시 추천 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "추천 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 금어기 해제 예정 어종 조회
     */
    @GetMapping("/upcoming-releases")
    public ResponseEntity<Map<String, Object>> getUpcomingReleases(
            @RequestParam(defaultValue = "30") int days) {
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(days);
            
            FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
            
            List<Map<String, Object>> upcomingReleases = config.getAllBanPeriods().stream()
                .filter(period -> {
                    try {
                        String[] endParts = period.getEndDate().split("-");
                        LocalDate banEndDate = LocalDate.of(config.getYear(), 
                            Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));
                        
                        if (period.isCrossYear() && banEndDate.isBefore(today)) {
                            banEndDate = banEndDate.plusYears(1);
                        }
                        
                        return banEndDate.isAfter(today) && !banEndDate.isAfter(endDate);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .map(period -> {
                    Map<String, Object> release = new HashMap<>();
                    release.put("fishName", period.getKoreanName());
                    release.put("scientificName", period.getScientificName());
                    release.put("region", period.getRegion());
                    release.put("releaseDate", period.getEndDate());
                    release.put("banType", period.getBanType());
                    release.put("description", period.getDescription());
                    
                    // D-day 계산
                    try {
                        String[] endParts = period.getEndDate().split("-");
                        LocalDate banEndDate = LocalDate.of(config.getYear(), 
                            Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));
                        if (period.isCrossYear() && banEndDate.isBefore(today)) {
                            banEndDate = banEndDate.plusYears(1);
                        }
                        release.put("dDay", java.time.temporal.ChronoUnit.DAYS.between(today, banEndDate));
                    } catch (NumberFormatException e) {
                        release.put("dDay", -1);
                    }
                    
                    return release;
                })
                .sorted((a, b) -> Long.compare((Long) a.get("dDay"), (Long) b.get("dDay")))
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("searchPeriod", days + "일");
            response.put("upcomingReleases", upcomingReleases);
            response.put("count", upcomingReleases.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("금어기 해제 예정 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "조회 실패: " + e.getMessage()));
        }
    }
    
    // 헬퍼 메서드들
    private boolean isDateInBanPeriod(LocalDate date, BanPeriodData period, int year) {
        try {
            String[] startParts = period.getStartDate().split("-");
            String[] endParts = period.getEndDate().split("-");
            
            LocalDate startDate = LocalDate.of(year, Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]));
            LocalDate endDate = LocalDate.of(year, Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));
            
            if (period.isCrossYear()) {
                endDate = endDate.plusYears(1);
                if (date.isBefore(startDate)) {
                    date = date.plusYears(1);
                }
            }
            
            return !date.isBefore(startDate) && !date.isAfter(endDate);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private int calculateRecommendationScore(List<BanPeriodData> fishPeriods, LocalDate fromDate, LocalDate toDate, int year) {
        int score = 100; // 기본 점수
        
        // 기간 내 금어기 일수 계산 (점수 차감)
        LocalDate currentDate = fromDate;
        int banDays = 0;
        int totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        
        while (!currentDate.isAfter(toDate)) {
            final LocalDate checkDate = currentDate;
            boolean isBanned = fishPeriods.stream()
                .anyMatch(period -> isDateInBanPeriod(checkDate, period, year));
            
            if (isBanned) banDays++;
            currentDate = currentDate.plusDays(1);
        }
        
        // 금어기 비율에 따른 점수 차감
        score -= (banDays * 50) / totalDays;
        
        // 지역 다양성 보너스
        long regionCount = fishPeriods.stream()
            .map(BanPeriodData::getRegion)
            .distinct()
            .count();
        score += Math.min(regionCount * 5, 20);
        
        // 크기 제한 있으면 약간 차감 (초보자 친화도)
        boolean hasSizeRestriction = fishPeriods.stream()
            .anyMatch(period -> period.getMinSize() != null);
        if (hasSizeRestriction) score -= 10;
        
        return Math.max(score, 0);
    }
}