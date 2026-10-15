package checkmate.insea.fish_info.model;

import java.time.LocalDate;

import checkmate.insea.fish_info.model.EnumClass.BanType;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fishing_ban_periods",
       indexes = {
           @Index(name = "idx_fishing_ban_periods_dates", columnList = "start_date, end_date"),
           @Index(name = "idx_fishing_ban_periods_fish_id", columnList = "fish_id")
       })
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class FishingBanPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long banId;

    @Column(name = "fish_id")
    private Long fishId;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "fish_id", insertable = false, updatable = false)
    private FishSpecies fishSpecies;

    @Column(name = "start_date", nullable=false)  
    private LocalDate startDate;

    @Column(name = "end_date", nullable=false)
    private LocalDate endDate;


    @Column(name = "region", length = 100)
    private String region;
    
    @Column(name = "ban_type", length = 50)
    @Enumerated(EnumType.STRING)
    private BanType banType;
    
    @Column(name = "min_size")
    private Integer minSize; // cm
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // 2024년 6월에 개정, 2025년 1월에 시행
    @Column(name = "year", nullable = false)
    private Integer year; 
}
