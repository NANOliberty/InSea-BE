package checkmate.insea.fish_info.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * 생물분류-중 정보와 SVG 아이콘을 관리하는 엔티티
 */
@Entity
@Table(name = "biological_category",
    indexes = {
        @Index(name = "idx_biological_category_name", columnList = "category_name"),
        @Index(name = "idx_biological_category_type", columnList = "category_type")
    }
)
public class BiologicalCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;
    
    /**
     * 생물분류-중 이름 (예: "어류", "세균", "연체동물" 등)
     */
    @Column(name = "category_name", nullable = false, length = 50, unique = true)
    private String categoryName;
    
    /**
     * 분류 타입 (marine, plant, micro 등)
     */
    @Column(name = "category_type", length = 20)
    private String categoryType;
    
    /**
     * SVG 아이콘 코드
     */
    @Column(name = "svg_icon", columnDefinition = "TEXT")
    private String svgIcon;
    
    /**
     * 아이콘 색상 정보 (CSS 클래스명 또는 색상 코드)
     */
    @Column(name = "icon_color", length = 50)
    private String iconColor;
    
    /**
     * 배경 그라디언트 정보
     */
    @Column(name = "background_gradient", length = 200)
    private String backgroundGradient;
    
    /**
     * 설명
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * 사용 여부
     */
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    /**
     * 표시 순서
     */
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 기본 생성자
    public BiologicalCategory() {}
    
    // 전체 생성자
    public BiologicalCategory(String categoryName, String categoryType, String svgIcon, 
                            String iconColor, String backgroundGradient, String description, 
                            Boolean isActive, Integer displayOrder) {
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.svgIcon = svgIcon;
        this.iconColor = iconColor;
        this.backgroundGradient = backgroundGradient;
        this.description = description;
        this.isActive = isActive;
        this.displayOrder = displayOrder;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getter and Setter methods
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getCategoryType() {
        return categoryType;
    }
    
    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }
    
    public String getSvgIcon() {
        return svgIcon;
    }
    
    public void setSvgIcon(String svgIcon) {
        this.svgIcon = svgIcon;
    }
    
    public String getIconColor() {
        return iconColor;
    }
    
    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }
    
    public String getBackgroundGradient() {
        return backgroundGradient;
    }
    
    public void setBackgroundGradient(String backgroundGradient) {
        this.backgroundGradient = backgroundGradient;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "BiologicalCategory{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", categoryType='" + categoryType + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", displayOrder=" + displayOrder +
                '}';
    }
}