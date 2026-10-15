package checkmate.insea.fishbio.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MarineBioApiResponse {
    
    @XmlElement(name = "header")
    private Header header;
    
    @XmlElement(name = "body")
    private Body body;
    
    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Header {
        @XmlElement(name = "ResultCode")
        private String resultCode;
        
        @XmlElement(name = "ResultMsg")
        private String resultMsg;
    }
    
    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Body {
        @XmlElement(name = "items")
        private Items items;
        
        @XmlElement(name = "numOfRows")
        private int numOfRows;
        
        @XmlElement(name = "pageNo")
        private int pageNo;
        
        @XmlElement(name = "totalCount")
        private int totalCount;
    }
    
    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Items {
        @XmlElement(name = "item")
        private List<Item> itemList;
    }
    
    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item {
        @XmlElement(name = "Kingdom")
        private String kingdom;
        
        @XmlElement(name = "KingdomKR")
        private String kingdomKR;
        
        @XmlElement(name = "PhylumDivision")
        private String phylumDivision;
        
        @XmlElement(name = "PhylumDivisionKR")
        private String phylumDivisionKR;
        
        @XmlElement(name = "Class")
        private String className;
        
        @XmlElement(name = "ClassKR")
        private String classKR;
        
        @XmlElement(name = "Order")
        private String orderName;
        
        @XmlElement(name = "OrderKR")
        private String orderKR;
        
        @XmlElement(name = "Family")
        private String family;
        
        @XmlElement(name = "FamilyKR")
        private String familyKR;
        
        @XmlElement(name = "SpcScitfNm")
        private String scientificName;
        
        @XmlElement(name = "CommKorNm")
        private String koreanName;
        
        @XmlElement(name = "SpcTyp")
        private String speciesType;
        
        @XmlElement(name = "ABST")
        private String abstractInfo;
        
        @XmlElement(name = "FORM")
        private String form;
        
        @XmlElement(name = "ECOL")
        private String ecology;
        
        @XmlElement(name = "CULTIVINF")
        private String cultivationInfo;
        
        @XmlElement(name = "BIOCHEMICAL")
        private String biochemical;
        
        @XmlElement(name = "ACTIVINFO")
        private String activeInfo;
        
        @XmlElement(name = "NADI")
        private String nationalDistribution;
        
        @XmlElement(name = "INDI")
        private String internationalDistribution;
        
        @XmlElement(name = "HABI")
        private String habitat;
        
        @XmlElement(name = "UTLZ")
        private String utilization;
        
        @XmlElement(name = "CorrNmTyp")
        private String correctionNameType;
        
        @XmlElement(name = "CorrSpcScitfNm")
        private String correctionScientificName;
    }
}
