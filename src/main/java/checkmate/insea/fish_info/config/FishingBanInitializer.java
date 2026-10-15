package checkmate.insea.fish_info.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import checkmate.insea.fish_info.service.FishingBanDataService;
import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션 시작 시 금어기 데이터 자동 로드를 위한 초기화 클래스
 */
@Component
@Slf4j
public class FishingBanInitializer {
    
    private final FishingBanDataService fishingBanDataService;
    private final FishingBanConfigValidator configValidator;
    private final FishingBanConfig fishingBanConfig;
    
    @Value("${fishing.ban.auto-load:true}")
    private boolean autoLoad;
    
    @Value("${fishing.ban.validate-on-startup:true}")
    private boolean validateOnStartup;
    
    public FishingBanInitializer(FishingBanDataService fishingBanDataService,
                                FishingBanConfigValidator configValidator,
                                FishingBanConfig fishingBanConfig) {
        this.fishingBanDataService = fishingBanDataService;
        this.configValidator = configValidator;
        this.fishingBanConfig = fishingBanConfig;
    }
    
    /**
     * 애플리케이션 시작 완료 시 실행
     */
    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        if (!autoLoad) {
            log.info("금어기 데이터 자동 로드가 비활성화되었습니다 (fishing.ban.auto-load=false)");
            return;
        }
        
        try {
            log.info("애플리케이션 시작 시 금어기 데이터 로드 시작");
            
            // 설정 파일 검증 (활성화된 경우)
            if (validateOnStartup) {
                configValidator.validateConfig(fishingBanConfig);
            }
            
            // 금어기 데이터 로드
            fishingBanDataService.saveFishingBanPeriodsFromConfig();
            
            log.info("애플리케이션 시작 시 금어기 데이터 로드 완료");
            
        } catch (Exception e) {
            log.error("애플리케이션 시작 시 금어기 데이터 로드 실패: {}", e.getMessage(), e);
            
            // 개발/테스트 환경에서는 애플리케이션을 중단하고, 운영에서는 경고만 로그
            String activeProfile = System.getProperty("spring.profiles.active", "");
            if (activeProfile.contains("dev") || activeProfile.contains("test")) {
                log.error("개발/테스트 환경에서 금어기 데이터 로드 실패로 애플리케이션을 종료합니다");
                System.exit(1);
            } else {
                log.warn("운영 환경에서 금어기 데이터 로드 실패, 기존 데이터를 사용합니다");
            }
        }
    }
    
    /**
     * 환경 설정 변경 시 실행 (Spring Cloud Config가 없으므로 주석 처리)
     */
    // Spring Cloud Config를 사용하지 않는 경우 이 메서드는 필요 없습니다
    /*
    @EventListener
    public void handleEnvironmentChange(EnvironmentChangeEvent event) {
        // 금어기 관련 설정이 변경된 경우에만 리로드
        boolean fishingConfigChanged = event.getKeys().stream()
                .anyMatch(key -> key.startsWith("fishing.ban"));
        
        if (fishingConfigChanged) {
            log.info("금어기 설정 변경 감지, 데이터 리로드 시작");
            try {
                fishingBanDataService.saveFishingBanPeriodsFromConfig();
                log.info("설정 변경에 따른 금어기 데이터 리로드 완료");
            } catch (Exception e) {
                log.error("설정 변경에 따른 금어기 데이터 리로드 실패: {}", e.getMessage(), e);
            }
        }
    }
    */
    
    /**
     * 수동으로 금어기 데이터를 리로드하는 메서드
     */
    public void manualReload() {
        log.info("수동 금어기 데이터 리로드 시작");
        try {
            if (validateOnStartup) {
                configValidator.validateConfig(fishingBanConfig);
            }
            fishingBanDataService.saveFishingBanPeriodsFromConfig();
            log.info("수동 금어기 데이터 리로드 완료");
        } catch (Exception e) {
            log.error("수동 금어기 데이터 리로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("금어기 데이터 리로드 실패", e);
        }
    }
}