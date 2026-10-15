package checkmate.insea.fish_info.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import checkmate.insea.fish_info.config.FishingBanConfig;
import checkmate.insea.fish_info.config.FishingBanInitializer;
import checkmate.insea.fish_info.service.FishingBanDataService;
import lombok.extern.slf4j.Slf4j;

/**
 * 금어기 관리를 위한 컨트롤러
 */
@RestController
@RequestMapping("/api/fishing-ban")
@Slf4j
public class FishingBanController {
    
    private final FishingBanDataService fishingBanDataService;
    private final FishingBanInitializer fishingBanInitializer;
    
    public FishingBanController(FishingBanDataService fishingBanDataService,
                               FishingBanInitializer fishingBanInitializer) {
        this.fishingBanDataService = fishingBanDataService;
        this.fishingBanInitializer = fishingBanInitializer;
    }
    
    /**
     * 현재 로드된 설정 정보 조회
     */
    @GetMapping("/config")
    public ResponseEntity<FishingBanConfig> getConfig() {
        FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
        return ResponseEntity.ok(config);
    }
    
    /**
     * 금어기 통계 정보 조회
     */
    @GetMapping("/statistics/{year}")
    public ResponseEntity<?> getStatistics(@PathVariable int year) {
        try {
            var statistics = fishingBanDataService.getFishingBanStatistics(year);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("통계 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "통계 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 설정 파일에서 금어기 데이터 수동 리로드
     */
    @PostMapping("/reload")
    public ResponseEntity<Map<String, Object>> reloadData() {
        try {
            log.info("금어기 데이터 수동 리로드 요청");
            fishingBanInitializer.manualReload();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "금어기 데이터 리로드 완료");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("금어기 데이터 리로드 실패: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "금어기 데이터 리로드 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 설정 파일 정보 요약 조회
     */
    @GetMapping("/config/summary")
    public ResponseEntity<Map<String, Object>> getConfigSummary() {
        try {
            FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("year", config.getYear());
            summary.put("lastUpdated", config.getLastUpdated());
            summary.put("fishSpecies", config.getFishBanPeriods().size());
            summary.put("crustaceanSpecies", config.getCrustaceanBanPeriods().size());
            summary.put("molluskSpecies", config.getMolluskBanPeriods().size());
            summary.put("seaweedSpecies", config.getSeaweedBanPeriods().size());
            summary.put("otherMarineSpecies", config.getOtherMarineBanPeriods().size());
            summary.put("totalSpecies", config.getAllBanPeriods().size());
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("설정 요약 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "설정 요약 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 헬스 체크 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            FishingBanConfig config = fishingBanDataService.getFishingBanConfig();
            health.put("status", "UP");
            health.put("configLoaded", config != null);
            health.put("year", config != null ? config.getYear() : null);
            health.put("totalBanPeriods", config != null ? config.getAllBanPeriods().size() : 0);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }
}