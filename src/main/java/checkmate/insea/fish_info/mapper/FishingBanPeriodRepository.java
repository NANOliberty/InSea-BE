package checkmate.insea.fish_info.mapper;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import checkmate.insea.fish_info.model.FishingBanPeriod;

@Repository
public interface FishingBanPeriodRepository extends JpaRepository<FishingBanPeriod, Long> {
    
    List<FishingBanPeriod> findByFishId(Long fishId);
    
    List<FishingBanPeriod> findByYear(Integer year);
    
    void deleteByYear(Integer year);
    
    @Query("SELECT bp FROM FishingBanPeriod bp WHERE bp.year = :year AND bp.startDate <= :currentDate AND bp.endDate >= :currentDate")
    List<FishingBanPeriod> findCurrentBanPeriods(@Param("year") Integer year, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT bp FROM FishingBanPeriod bp WHERE bp.fishId = :fishId AND bp.year = :year")
    List<FishingBanPeriod> findByFishIdAndYear(@Param("fishId") Long fishId, @Param("year") Integer year);
    
    @Query("SELECT bp FROM FishingBanPeriod bp WHERE bp.region LIKE %:region% AND bp.year = :year")
    List<FishingBanPeriod> findByRegionAndYear(@Param("region") String region, @Param("year") Integer year);

    // FishingBanPeriodRepository.java에 추가할 메서드들

/**
 * 특정 연도의 모든 금어기 정보 조회
 */
List<FishingBanPeriod> findByYear(int year);

/**
 * 특정 연도의 데이터 삭제 후 삭제된 개수 반환
 */
@Modifying
@Transactional
@Query("DELETE FROM FishingBanPeriod f WHERE f.year = :year")
int deleteByYear(@Param("year") int year);

}