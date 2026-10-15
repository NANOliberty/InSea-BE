package checkmate.insea.fish_info.config;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import checkmate.insea.fish_info.model.EnumClass.BanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 개별 금어기 정보를 담는 데이터 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BanPeriodData {
    private String koreanName;
    private String scientificName;
    private String startDate;  // MM-dd 형식
    private String endDate;    // MM-dd 형식
    private boolean crossYear; // 연도를 넘나드는 금어기 여부
    private String region;
    private BanType banType;
    private Integer minSize;
    private String description;
    
    /**
     * 시작일을 LocalDate로 변환
     */
    public LocalDate getStartDateAsLocalDate(int year) {
        String[] parts = startDate.split("-");
        int month = Integer.parseInt(parts[0]);
        int day = Integer.parseInt(parts[1]);
        return LocalDate.of(year, month, day);
    }
    
    /**
     * 종료일을 LocalDate로 변환 (연도 넘나드는 경우 처리)
     */
    public LocalDate getEndDateAsLocalDate(int year) {
        String[] parts = endDate.split("-");
        int month = Integer.parseInt(parts[0]);
        int day = Integer.parseInt(parts[1]);
        
        // 연도를 넘나드는 경우 종료일에 1년 추가
        if (crossYear) {
            return LocalDate.of(year + 1, month, day);
        }
        return LocalDate.of(year, month, day);
    }
}