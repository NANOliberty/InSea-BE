package checkmate.insea.fishbio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FishInfoDto {
    private Long id;
    private String kingdom;
    private String kingdomKR;
    private String phylumDivision;
    private String phylumDivisionKR;
    private String className;
    private String classKR;
    private String orderName;
    private String orderKR;
    private String family;
    private String familyKR;
    private String scientificName;
    private String koreanName;
    private String speciesType;
    private String abstractInfo;
    private String form;
    private String ecology;
    private String cultivationInfo;
    private String biochemical;
    private String activeInfo;
    private String nationalDistribution;
    private String internationalDistribution;
    private String habitat;
    private String utilization;
    private String correctionNameType;
    private String correctionScientificName;
}