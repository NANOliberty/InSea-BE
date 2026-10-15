package checkmate.insea.fishbio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fish_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FishInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fish_info_seq")
    @SequenceGenerator(name = "fish_info_seq", sequenceName = "fish_info_seq", allocationSize = 1)
    private Long id;
    
    @Column(name = "kingdom", length = 500)
    private String kingdom; // 계
    
    @Column(name = "kingdom_kr", length = 500)
    private String kingdomKR; // 계(한국어)
    
    @Column(name = "phylum_division", length = 500)
    private String phylumDivision; // 문/부
    
    @Column(name = "phylum_division_kr", length = 500)
    private String phylumDivisionKR; // 문/부(한국어)
    
    @Column(name = "class_name", length = 500)
    private String className; // 강
    
    @Column(name = "class_kr", length = 500)
    private String classKR; // 강(한국어)
    
    @Column(name = "order_name", length = 500)
    private String orderName; // 목
    
    @Column(name = "order_kr", length = 500)
    private String orderKR; // 목(한국어)
    
    @Column(name = "family", length = 500)
    private String family; // 과
    
    @Column(name = "family_kr", length = 500)
    private String familyKR; // 과(한국어)
    
    @Column(name = "scientific_name", unique = true, length = 1000)
    private String scientificName; // 학명
    
    @Column(name = "korean_name", length = 500)
    private String koreanName; // 국명
    
    @Column(name = "species_type", length = 500)
    private String speciesType; // 종 유형
    
    @Column(name = "abstract", columnDefinition = "TEXT")
    private String abstractInfo; // 요약
    
    @Column(name = "form", columnDefinition = "TEXT")
    private String form; // 형태
    
    @Column(name = "ecology", columnDefinition = "TEXT")
    private String ecology; // 생태
    
    @Column(name = "cultivation_info", columnDefinition = "TEXT")
    private String cultivationInfo; // 양식정보
    
    @Column(name = "biochemical", columnDefinition = "TEXT")
    private String biochemical; // 생화학
    
    @Column(name = "active_info", length = 1000)
    private String activeInfo; // 활성정보
    
    @Column(name = "national_distribution", length = 1000)
    private String nationalDistribution; // 국내분포
    
    @Column(name = "international_distribution", length = 1000)
    private String internationalDistribution; // 국외분포
    
    @Column(name = "habitat", length = 1000)
    private String habitat; // 서식지
    
    @Column(name = "utilization", length = 1000)
    private String utilization; // 이용
    
    @Column(name = "correction_name_type", length = 500)
    private String correctionNameType; // 정정명타입
    
    @Column(name = "correction_scientific_name", length = 1000)
    private String correctionScientificName; // 정정학명
}