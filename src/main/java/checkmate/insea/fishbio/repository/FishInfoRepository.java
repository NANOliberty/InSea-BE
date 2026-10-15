package checkmate.insea.fishbio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import checkmate.insea.fishbio.entity.FishInfo;

@Repository
public interface FishInfoRepository extends JpaRepository<FishInfo, Long> {
    
    // 학명으로 검색
    Optional<FishInfo> findByScientificName(String scientificName);
    
    // 한국명으로 검색 (부분 일치)
    List<FishInfo> findByKoreanNameContaining(String koreanName);
    
    // 과(Family)별 검색
    List<FishInfo> findByFamilyKR(String familyKR);
    
    // 서식지별 검색
    List<FishInfo> findByHabitatContaining(String habitat);
    
    // 키워드 통합 검색
    @Query("SELECT f FROM FishInfo f WHERE " +
           "f.koreanName LIKE %:keyword% OR " +
           "f.scientificName LIKE %:keyword% OR " +
           "f.familyKR LIKE %:keyword% OR " +
           "f.habitat LIKE %:keyword%")
    Page<FishInfo> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 학명 존재 여부 확인
    boolean existsByScientificName(String scientificName);
}