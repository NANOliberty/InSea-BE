package checkmate.insea.fish_info.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seasonal_fish", 
       indexes = @Index(name = "idx_seasonal_fish_month", columnList = "month"),
       uniqueConstraints = @UniqueConstraint(columnNames = {"fish_id", "month"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalFish {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seasonal_id")
    private Long seasonalId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fish_id", nullable = false)
    private FishSpecies fishSpecies;
    
    @Column(name = "month", nullable = false)
    @jakarta.validation.constraints.Min(1)
    @jakarta.validation.constraints.Max(12)
    private Integer month;
    
    @Column(name = "is_best_season")
    @Builder.Default
    private Boolean isBestSeason = false;
    
    @Column(name = "weather_condition", length = 100)
    private String weatherCondition;
}