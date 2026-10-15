package checkmate.insea.fish_info.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import checkmate.insea.fish_info.model.EnumClass.BanType;
import lombok.extern.slf4j.Slf4j;

/**
 * 금어기 설정 파일 검증을 위한 Validator
 */
@Component
@Slf4j
public class FishingBanConfigValidator {
    
    /**
     * 설정 파일 전체 검증
     */
    public void validateConfig(FishingBanConfig config) {
        log.info("금어기 설정 파일 검증 시작");
        
        List<String> errors = new ArrayList<>();
        
        // 연도 검증
        if (config.getYear() < 2020 || config.getYear() > 2030) {
            errors.add("유효하지 않은 연도: " + config.getYear());
        }
        
        // 각 카테고리별 데이터 검증
        validateBanPeriods("어류", config.getFishBanPeriods(), errors);
        validateBanPeriods("갑각류", config.getCrustaceanBanPeriods(), errors);
        validateBanPeriods("패류", config.getMolluskBanPeriods(), errors);
        validateBanPeriods("해조류", config.getSeaweedBanPeriods(), errors);
        validateBanPeriods("기타수산자원", config.getOtherMarineBanPeriods(), errors);
        
        if (!errors.isEmpty()) {
            String errorMessage = "설정 파일 검증 실패:\n" + String.join("\n", errors);
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        log.info("금어기 설정 파일 검증 완료");
    }
    
    /**
     * 개별 금어기 데이터 목록 검증
     */
    private void validateBanPeriods(String category, List<BanPeriodData> periods, List<String> errors) {
        for (int i = 0; i < periods.size(); i++) {
            BanPeriodData period = periods.get(i);
            String prefix = String.format("[%s-%d번째] %s: ", category, i + 1, period.getKoreanName());
            
            validateSingleBanPeriod(period, prefix, errors);
        }
    }
    
    /**
     * 개별 금어기 데이터 검증
     */
    private void validateSingleBanPeriod(BanPeriodData period, String prefix, List<String> errors) {
        // 필수 필드 검증
        if (isNullOrEmpty(period.getKoreanName())) {
            errors.add(prefix + "한국명이 비어있습니다");
        }
        
        if (isNullOrEmpty(period.getScientificName())) {
            errors.add(prefix + "학명이 비어있습니다");
        }
        
        if (isNullOrEmpty(period.getRegion())) {
            errors.add(prefix + "지역이 비어있습니다");
        }
        
        if (isNullOrEmpty(period.getDescription())) {
            errors.add(prefix + "설명이 비어있습니다");
        }
        
        // 날짜 형식 검증
        if (period.getStartDate() == null || !isValidDateFormat(period.getStartDate())) {
            errors.add(prefix + "시작일 형식이 잘못되었습니다 (MM-dd 형식 필요)");
        }
        
        if (period.getEndDate() == null || !isValidDateFormat(period.getEndDate())) {
            errors.add(prefix + "종료일 형식이 잘못되었습니다 (MM-dd 형식 필요)");
        }
        
        // 금어기 타입 검증
        if (period.getBanType() == null) {
            errors.add(prefix + "금어기 타입이 지정되지 않았습니다");
        }
        
        // 크기 제한 타입인 경우 최소 크기 필수
        if (period.getBanType() == BanType.SIZE_RESTRICTION && period.getMinSize() == null) {
            errors.add(prefix + "크기 제한 타입이지만 최소 크기가 지정되지 않았습니다");
        }
        
        // 크기 값 유효성 검증
        if (period.getMinSize() != null && period.getMinSize() <= 0) {
            errors.add(prefix + "최소 크기는 0보다 커야 합니다");
        }
        
        // 날짜 논리 검증 (연도를 넘나드지 않는 경우)
        if (!period.isCrossYear() && period.getStartDate() != null && period.getEndDate() != null) {
            if (isValidDateFormat(period.getStartDate()) && isValidDateFormat(period.getEndDate())) {
                if (compareDates(period.getStartDate(), period.getEndDate()) > 0) {
                    errors.add(prefix + "시작일이 종료일보다 늦습니다");
                }
            }
        }
    }
    
    /**
     * 문자열이 null이거나 비어있는지 확인
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 날짜 형식이 MM-dd 형식인지 검증
     */
    private boolean isValidDateFormat(String date) {
        try {
            String[] parts = date.split("-");
            if (parts.length != 2) return false;
            
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            
            if (month < 1 || month > 12 || day < 1) {  
                return false;  
            }  
                        
            // 월별 최대 일수 확인  
            int maxDays = switch (month) {  
                case 2 -> 29;  // 윤년 고려하여 2월은 29일까지 허용  
                case 4, 6, 9, 11 -> 30;  
                default -> 31;  
            };  

            return day <= maxDays;  
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 두 날짜를 비교 (MM-dd 형식)
     * @return 첫 번째 날짜가 더 크면 양수, 같으면 0, 작으면 음수
     */
    private int compareDates(String date1, String date2) {
        String[] parts1 = date1.split("-");
        String[] parts2 = date2.split("-");
        
        int month1 = Integer.parseInt(parts1[0]);
        int day1 = Integer.parseInt(parts1[1]);
        int month2 = Integer.parseInt(parts2[0]);
        int day2 = Integer.parseInt(parts2[1]);
        
        if (month1 != month2) {
            return month1 - month2;
        }
        return day1 - day2;
    }
}