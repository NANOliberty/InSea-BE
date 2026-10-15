package checkmate.insea.fish_info.mapper;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import checkmate.insea.fish_info.model.BiologicalCategory;

@Repository
public interface BiologicalCategoryRepository extends JpaRepository<BiologicalCategory, Long> {
    
    /**
     * 카테고리명으로 검색
     */
    Optional<BiologicalCategory> findByCategoryName(String categoryName);
    
    /**
     * 카테고리명 존재 여부 확인
     */
    boolean existsByCategoryName(String categoryName);
    
    /**
     * 활성화된 카테고리 목록 조회 (표시 순서대로)
     */
    List<BiologicalCategory> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    /**
     * 카테고리 타입별 조회
     */
    List<BiologicalCategory> findByCategoryTypeOrderByDisplayOrderAsc(String categoryType);
    
    /**
     * 카테고리명에 키워드가 포함된 항목 검색
     */
    @Query("SELECT bc FROM BiologicalCategory bc WHERE bc.categoryName LIKE %:keyword% AND bc.isActive = true")
    List<BiologicalCategory> findByCategoryNameContaining(@Param("keyword") String keyword);
    
    /**
     * 카테고리별 어류 개수 통계
     */
    @Query("SELECT bc.categoryName, COUNT(fs) " +
           "FROM BiologicalCategory bc LEFT JOIN FishSpecies fs ON bc.categoryId = fs.biologicalCategory.categoryId " +
           "WHERE bc.isActive = true " +
           "GROUP BY bc.categoryId, bc.categoryName " +
           "ORDER BY COUNT(fs) DESC")
    List<Object[]> countFishByCategory();
}