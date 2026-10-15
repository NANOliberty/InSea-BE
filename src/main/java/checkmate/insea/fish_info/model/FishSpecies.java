package checkmate.insea.fish_info.model;

import java.time.LocalDateTime;

import checkmate.insea.fish_info.model.EnumClass.FishRarity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "fish_species",
    indexes = {
        @Index(name = "idx_fish_species_korean_name", columnList = "korean_name"),
        @Index(name = "idx_fish_species_category", columnList = "category_id")
    })
public class FishSpecies {

    public static Object builder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fishId;

    @Column(name = "korean_name", nullable = false, length = 100)
    private String koreanName;

    @Column(name = "english_name", length = 100)
    private String englishName;

    @Column(name = "scientific_name", length = 200)
    private String scientificName;
    
    /**
     * 생물분류-중과의 연관관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private BiologicalCategory biologicalCategory;
    
    /**
     * 원본 생물분류-중 텍스트 (백업용)
     */
    @Column(name = "biological_classification_middle", length = 50)
    private String biologicalClassificationMiddle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "habitat", columnDefinition = "TEXT")
    private String habitat;

    @Min(value = 0, message = "크기는 0 이상이어야 합니다")
    @Column(name = "size_min")
    private Integer sizeMin; // cm

    @Min(value = 0, message = "크기는 0 이상이어야 합니다")
    @Column(name = "size_max")
    private Integer sizeMax; // cm

    @Column(name = "weight_min")
    private Integer weightMin; // g

    @Column(name = "weight_max")
    private Integer weightMax; // g

    @Column(name = "best_season", length = 50)
    private String bestSeason;

    @Column(name = "best_time", length = 50)
    private String bestTime;

    @Column(name = "best_bait", columnDefinition = "TEXT")
    private String bestBait;

    @Column(name = "fishing_method", columnDefinition = "TEXT")
    private String fishingMethod;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "rarity", length = 20)
    @Enumerated(EnumType.STRING)
    private FishRarity rarity;

    // 분류 정보
    @Column(name = "kingdom", length = 50)
    private String kingdom; // 계

    @Column(name = "phylum", length = 50)
    private String phylum; // 문

    @Column(name = "class_name", length = 50)
    private String className; // 강

    @Column(name = "order_name", length = 50)
    private String orderName; // 목

    @Column(name = "family", length = 50)
    private String family; // 과

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 기본 생성자
    public FishSpecies() {}

    // 전체 생성자
    public FishSpecies(String koreanName, String englishName, String scientificName,
                      BiologicalCategory biologicalCategory, String biologicalClassificationMiddle,
                      String description, String habitat, Integer sizeMin, Integer sizeMax,
                      Integer weightMin, Integer weightMax, String bestSeason, String bestTime,
                      String bestBait, String fishingMethod, String imageUrl, FishRarity rarity,
                      String kingdom, String phylum, String className, String orderName, String family) {
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.scientificName = scientificName;
        this.biologicalCategory = biologicalCategory;
        this.biologicalClassificationMiddle = biologicalClassificationMiddle;
        this.description = description;
        this.habitat = habitat;
        this.sizeMin = sizeMin;
        this.sizeMax = sizeMax;
        this.weightMin = weightMin;
        this.weightMax = weightMax;
        this.bestSeason = bestSeason;
        this.bestTime = bestTime;
        this.bestBait = bestBait;
        this.fishingMethod = fishingMethod;
        this.imageUrl = imageUrl;
        this.rarity = rarity;
        this.kingdom = kingdom;
        this.phylum = phylum;
        this.className = className;
        this.orderName = orderName;
        this.family = family;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getter and Setter methods
    public Long getFishId() {
        return fishId;
    }

    public void setFishId(Long fishId) {
        this.fishId = fishId;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public void setKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public BiologicalCategory getBiologicalCategory() {
        return biologicalCategory;
    }

    public void setBiologicalCategory(BiologicalCategory biologicalCategory) {
        this.biologicalCategory = biologicalCategory;
    }

    public String getBiologicalClassificationMiddle() {
        return biologicalClassificationMiddle;
    }

    public void setBiologicalClassificationMiddle(String biologicalClassificationMiddle) {
        this.biologicalClassificationMiddle = biologicalClassificationMiddle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public Integer getSizeMin() {
        return sizeMin;
    }

    public void setSizeMin(Integer sizeMin) {
        this.sizeMin = sizeMin;
    }

    public Integer getSizeMax() {
        return sizeMax;
    }

    public void setSizeMax(Integer sizeMax) {
        this.sizeMax = sizeMax;
    }

    public Integer getWeightMin() {
        return weightMin;
    }

    public void setWeightMin(Integer weightMin) {
        this.weightMin = weightMin;
    }

    public Integer getWeightMax() {
        return weightMax;
    }

    public void setWeightMax(Integer weightMax) {
        this.weightMax = weightMax;
    }

    public String getBestSeason() {
        return bestSeason;
    }

    public void setBestSeason(String bestSeason) {
        this.bestSeason = bestSeason;
    }

    public String getBestTime() {
        return bestTime;
    }

    public void setBestTime(String bestTime) {
        this.bestTime = bestTime;
    }

    public String getBestBait() {
        return bestBait;
    }

    public void setBestBait(String bestBait) {
        this.bestBait = bestBait;
    }

    public String getFishingMethod() {
        return fishingMethod;
    }

    public void setFishingMethod(String fishingMethod) {
        this.fishingMethod = fishingMethod;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public FishRarity getRarity() {
        return rarity;
    }

    public void setRarity(FishRarity rarity) {
        this.rarity = rarity;
    }

    public String getKingdom() {
        return kingdom;
    }

    public void setKingdom(String kingdom) {
        this.kingdom = kingdom;
    }

    public String getPhylum() {
        return phylum;
    }

    public void setPhylum(String phylum) {
        this.phylum = phylum;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "FishSpecies{" +
                "fishId=" + fishId +
                ", koreanName='" + koreanName + '\'' +
                ", scientificName='" + scientificName + '\'' +
                ", biologicalClassificationMiddle='" + biologicalClassificationMiddle + '\'' +
                ", family='" + family + '\'' +
                ", rarity=" + rarity +
                '}';
    }
}