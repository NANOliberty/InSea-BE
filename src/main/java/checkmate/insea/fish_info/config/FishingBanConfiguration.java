package checkmate.insea.fish_info.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * 금어기 설정을 위한 Configuration 클래스
 */
@Configuration
@EnableConfigurationProperties
@Slf4j
public class FishingBanConfiguration {
    
    @Bean
    @Primary
    public FishingBanConfig fishingBanConfig(@Value("${fishing.ban.config-file:classpath:templates/fishing-ban-data-2025.json}") 
                                           Resource configFile) throws IOException {
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        try (InputStream inputStream = configFile.getInputStream()) {
            FishingBanConfig config = objectMapper.readValue(inputStream, FishingBanConfig.class);
            log.info("금어기 설정 파일 로드 완료: {} 어종, 최종 업데이트: {}", 
                    getTotalSpeciesCount(config), config.getLastUpdated());
            return config;
        }
    }
    
    private int getTotalSpeciesCount(FishingBanConfig config) {
        return config.getFishBanPeriods().size() + 
               config.getCrustaceanBanPeriods().size() + 
               config.getMolluskBanPeriods().size() + 
               config.getSeaweedBanPeriods().size() + 
               config.getOtherMarineBanPeriods().size();
    }
}