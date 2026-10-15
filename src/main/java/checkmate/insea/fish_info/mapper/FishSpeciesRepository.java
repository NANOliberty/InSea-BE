package checkmate.insea.fish_info.mapper;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import checkmate.insea.fish_info.model.FishSpecies;

@Repository
public interface FishSpeciesRepository extends JpaRepository<FishSpecies, Long> {
    
    boolean existsByScientificName(String scientificName);
    
    /**
     * 한국명 또는 학명으로 검색
     */
    @Query("SELECT f FROM FishSpecies f WHERE f.koreanName = :koreanName OR f.scientificName = :scientificName")
    Optional<FishSpecies> findByKoreanNameOrScientificName(
        @Param("koreanName") String koreanName,
        @Param("scientificName") String scientificName);
    
    List<FishSpecies> findByKoreanNameContaining(String koreanName);
    
    /**
     * 한국명으로 어류 검색
     */
    Optional<FishSpecies> findByKoreanName(String koreanName);
    
    /**
     * 한국명 존재 여부 확인
     */
    boolean existsByKoreanName(String koreanName);
    
    /**
     * 학명으로 어류 검색
     */
    Optional<FishSpecies> findByScientificName(String scientificName);
    
    /**
     * 한국명에 특정 키워드가 포함된 어류 검색
     */
    @Query("SELECT f FROM FishSpecies f WHERE f.koreanName LIKE %:keyword%")
    List<FishSpecies> findByKoreanNameContainingByKeyword(@Param("keyword") String keyword);
    
    /**
     * 과(Family)로 어류 검색
     */
    List<FishSpecies> findByFamily(String family);
    
    /**
     * 목(Order)으로 어류 검색
     */
    List<FishSpecies> findByOrderName(String orderName);
    
    /**
     * 강(Class)으로 어류 검색
     */
    List<FishSpecies> findByClassName(String className);
    
    /**
     * 희귀도별 어류 검색
     */
    List<FishSpecies> findByRarity(checkmate.insea.fish_info.model.EnumClass.FishRarity rarity);
    
    /**
     * 생물분류-중 카테고리별 어류 검색
     */
    List<FishSpecies> findByBiologicalCategory_CategoryName(String categoryName);
    
    /**
     * 생물분류-중 카테고리 ID별 어류 검색
     */
    List<FishSpecies> findByBiologicalCategory_CategoryId(Long categoryId);
    
    /**
     * 생물분류-중 카테고리 타입별 어류 검색
     */
    @Query("SELECT f FROM FishSpecies f WHERE f.biologicalCategory.categoryType = :categoryType")
    List<FishSpecies> findByBiologicalCategoryType(@Param("categoryType") String categoryType);
    
    /**
     * 전체 어류 수 조회
     */
    @Query("SELECT COUNT(f) FROM FishSpecies f")
    long countAllFish();
    
    /**
     * 과별 어류 수 통계
     */
    @Query("SELECT f.family, COUNT(f) FROM FishSpecies f WHERE f.family IS NOT NULL GROUP BY f.family ORDER BY COUNT(f) DESC")
    List<Object[]> countByFamily();
    
    /**
     * 목별 어류 수 통계
     */
    @Query("SELECT f.orderName, COUNT(f) FROM FishSpecies f WHERE f.orderName IS NOT NULL GROUP BY f.orderName ORDER BY COUNT(f) DESC")
    List<Object[]> countByOrder();
    
    /**
     * 생물분류-중 카테고리별 어류 수 통계
     */
    @Query("SELECT COALESCE(bc.categoryName, '미분류'), COUNT(f) " +
           "FROM FishSpecies f " +
           "LEFT JOIN f.biologicalCategory bc " +
           "GROUP BY bc.categoryId, bc.categoryName " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> countByBiologicalCategory();
    
    /**
     * 생물분류-중 카테고리 타입별 어류 수 통계
     */
    @Query("SELECT COALESCE(bc.categoryType, 'unknown'), COUNT(f) " +
           "FROM FishSpecies f " +
           "LEFT JOIN f.biologicalCategory bc " +
           "GROUP BY bc.categoryType " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> countByBiologicalCategoryType();
    
    /**
     * 카테고리가 없는 어류 조회
     */
    List<FishSpecies> findByBiologicalCategoryIsNull();
    
    /**
     * 특정 카테고리와 과(Family) 조합으로 검색
     */
    @Query("SELECT f FROM FishSpecies f WHERE f.biologicalCategory.categoryName = :categoryName AND f.family = :family")
    List<FishSpecies> findByCategoryNameAndFamily(@Param("categoryName") String categoryName, @Param("family") String family);
    
    /**
     * 생물분류-중 텍스트로 검색 (카테고리 연관관계가 없는 경우)
     */
    List<FishSpecies> findByBiologicalClassificationMiddle(String biologicalClassificationMiddle);
    
    /**
     * 생물분류-중 텍스트에 키워드가 포함된 어류 검색
     */
    @Query("SELECT f FROM FishSpecies f WHERE f.biologicalClassificationMiddle LIKE %:keyword%")
    List<FishSpecies> findByBiologicalClassificationMiddleContaining(@Param("keyword") String keyword);
    
    /**
     * 복합 검색: 한국명, 학명, 생물분류 모두 포함
     */
    @Query("SELECT f FROM FishSpecies f WHERE " +
           "f.koreanName LIKE %:keyword% OR " +
           "f.scientificName LIKE %:keyword% OR " +
           "f.biologicalClassificationMiddle LIKE %:keyword% OR " +
           "f.family LIKE %:keyword%")
    List<FishSpecies> searchByMultipleFields(@Param("keyword") String keyword);
    
    /**
     * 크기 범위로 검색
     */
    @Query("SELECT f FROM FishSpecies f WHERE " +
           "(f.sizeMin IS NULL OR f.sizeMin >= :minSize) AND " +
           "(f.sizeMax IS NULL OR f.sizeMax <= :maxSize)")
    List<FishSpecies> findBySizeRange(@Param("minSize") Integer minSize, @Param("maxSize") Integer maxSize);
    
    /**
     * 무게 범위로 검색
     */
    @Query("SELECT f FROM FishSpecies f WHERE " +
           "(f.weightMin IS NULL OR f.weightMin >= :minWeight) AND " +
           "(f.weightMax IS NULL OR f.weightMax <= :maxWeight)")
    List<FishSpecies> findByWeightRange(@Param("minWeight") Integer minWeight, @Param("maxWeight") Integer maxWeight);
    
    /**
     * 계통분류별 통계 (계-문-강-목-과)
     */
    @Query("SELECT f.kingdom, COUNT(f) FROM FishSpecies f WHERE f.kingdom IS NOT NULL GROUP BY f.kingdom ORDER BY COUNT(f) DESC")
    List<Object[]> countByKingdom();
    
    @Query("SELECT f.phylum, COUNT(f) FROM FishSpecies f WHERE f.phylum IS NOT NULL GROUP BY f.phylum ORDER BY COUNT(f) DESC")
    List<Object[]> countByPhylum();
    
    @Query("SELECT f.className, COUNT(f) FROM FishSpecies f WHERE f.className IS NOT NULL GROUP BY f.className ORDER BY COUNT(f) DESC")
    List<Object[]> countByClassName();
    
    /**
     * 희귀도별 통계
     */
    @Query("SELECT f.rarity, COUNT(f) FROM FishSpecies f GROUP BY f.rarity ORDER BY COUNT(f) DESC")
    List<Object[]> countByRarity();
    
    /**
     * 최근 등록된 어류 조회
     */
    @Query("SELECT f FROM FishSpecies f ORDER BY f.createdAt DESC")
    List<FishSpecies> findRecentlyAdded();
    
    /**
     * 최근 등록된 어류 조회 (개수 제한)
     */
    @Query("SELECT f FROM FishSpecies f ORDER BY f.createdAt DESC LIMIT :limit")
    List<FishSpecies> findRecentlyAdded(@Param("limit") int limit);
    
    /**
     * 생물분류별로 최근 등록된 어류 조회
     */
    @Query("SELECT f FROM FishSpecies f WHERE f.biologicalCategory.categoryName = :categoryName ORDER BY f.createdAt DESC LIMIT :limit")
    List<FishSpecies> findRecentlyAddedByCategory(@Param("categoryName") String categoryName, @Param("limit") int limit);
    
    /**
     * 데이터 완성도 체크 - 필수 필드가 누락된 어류
     */
    @Query("SELECT f FROM FishSpecies f WHERE " +
           "f.koreanName IS NULL OR f.koreanName = '' OR " +
           "f.scientificName IS NULL OR f.scientificName = '' OR " +
           "f.biologicalCategory IS NULL")
    List<FishSpecies> findIncompleteData();
    
    /**
     * 중복 데이터 체크 - 같은 한국명을 가진 어류들
     */
    @Query("SELECT f.koreanName, COUNT(f) FROM FishSpecies f GROUP BY f.koreanName HAVING COUNT(f) > 1")
    List<Object[]> findDuplicateKoreanNames();
    
    /**
     * 중복 데이터 체크 - 같은 학명을 가진 어류들
     */
    @Query("SELECT f.scientificName, COUNT(f) FROM FishSpecies f WHERE f.scientificName IS NOT NULL GROUP BY f.scientificName HAVING COUNT(f) > 1")
    List<Object[]> findDuplicateScientificNames();
}