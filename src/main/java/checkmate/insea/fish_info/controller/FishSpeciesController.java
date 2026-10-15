package checkmate.insea.fish_info.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import checkmate.insea.fish_info.mapper.FishSpeciesRepository;
import checkmate.insea.fish_info.model.FishSpecies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/fish-species")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FishSpeciesController {

    private final FishSpeciesRepository fishSpeciesRepository;

    /**
     * 전체 어류 목록 조회 (페이징, 생물분류 카테고리 정보 포함)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFishSpecies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<FishSpecies> fishPage = fishSpeciesRepository.findAll(pageable);
            
            // FishSpecies 데이터를 DTO로 변환하여 생물분류 카테고리 정보 포함
            List<Map<String, Object>> fishData = fishPage.getContent().stream()
                .map(this::convertToDto)
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", fishData);
            response.put("currentPage", page);
            response.put("totalPages", fishPage.getTotalPages());
            response.put("totalElements", fishPage.getTotalElements());
            response.put("hasNext", fishPage.hasNext());
            response.put("hasPrevious", fishPage.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "데이터 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 어류 검색 (한국명 기준, 카테고리 정보 포함)
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchFishSpecies(@RequestParam String q) {
        try {
            List<FishSpecies> results = fishSpeciesRepository.findByKoreanNameContaining(q);
            
            List<Map<String, Object>> fishData = results.stream()
                .map(this::convertToDto)
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "data", fishData,
                "count", results.size(),
                "searchTerm", q
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "검색 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 특정 어류 상세 조회 (카테고리 정보 포함)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFishSpeciesById(@PathVariable Long id) {
        try {
            Optional<FishSpecies> fish = fishSpeciesRepository.findById(id);
            if (fish.isPresent()) {
                return ResponseEntity.ok(Map.of("data", convertToDto(fish.get())));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 생물분류-중별 어류 조회
     */
    @GetMapping("/by-category/{categoryName}")
    public ResponseEntity<Map<String, Object>> getFishSpeciesByCategory(@PathVariable String categoryName) {
        try {
            List<FishSpecies> results = fishSpeciesRepository.findByBiologicalCategory_CategoryName(categoryName);
            
            List<Map<String, Object>> fishData = results.stream()
                .map(this::convertToDto)
                .toList();
                
            return ResponseEntity.ok(Map.of(
                "data", fishData,
                "count", results.size(),
                "categoryName", categoryName
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "카테고리별 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 과(Family)별 어류 조회
     */
    @GetMapping("/by-family/{family}")
    public ResponseEntity<Map<String, Object>> getFishSpeciesByFamily(@PathVariable String family) {
        try {
            List<FishSpecies> results = fishSpeciesRepository.findByFamily(family);
            
            List<Map<String, Object>> fishData = results.stream()
                .map(this::convertToDto)
                .toList();
                
            return ResponseEntity.ok(Map.of(
                "data", fishData,
                "count", results.size(),
                "family", family
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "과별 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 분류 통계 조회 (생물분류-중 포함)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getFishStatistics() {
        try {
            long totalCount = fishSpeciesRepository.count();
            List<Object[]> familyStats = fishSpeciesRepository.countByFamily();
            List<Object[]> orderStats = fishSpeciesRepository.countByOrder();
            List<Object[]> categoryStats = fishSpeciesRepository.countByBiologicalCategory();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCount", totalCount);
            stats.put("familyCount", familyStats.size());
            stats.put("orderCount", orderStats.size());
            stats.put("categoryCount", categoryStats.size());
            stats.put("topFamilies", familyStats.subList(0, Math.min(10, familyStats.size())));
            stats.put("topOrders", orderStats.subList(0, Math.min(10, orderStats.size())));
            stats.put("categoryDistribution", categoryStats);
            
            return ResponseEntity.ok(Map.of("data", stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "통계 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 어류 총 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getFishCount() {
        try {
            long count = fishSpeciesRepository.count();
            return ResponseEntity.ok(Map.of(
                "count", count,
                "message", count + "개의 어류 데이터가 있습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "개수 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 서버 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            long count = fishSpeciesRepository.count();
            return ResponseEntity.ok(Map.of(
                "status", "OK",
                "fishCount", count,
                "message", "어류 데이터 API가 정상 작동 중입니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "ERROR",
                "message", "데이터베이스 연결에 문제가 있습니다."
            ));
        }
    }

    /**
     * FishSpecies 엔티티를 DTO로 변환 (생물분류 카테고리 정보 포함)
     */
    private Map<String, Object> convertToDto(FishSpecies fish) {
        Map<String, Object> dto = new HashMap<>();
        
        // 기본 어류 정보
        dto.put("fishId", fish.getFishId());
        dto.put("koreanName", fish.getKoreanName());
        dto.put("englishName", fish.getEnglishName());
        dto.put("scientificName", fish.getScientificName());
        dto.put("description", fish.getDescription());
        dto.put("habitat", fish.getHabitat());
        dto.put("sizeMin", fish.getSizeMin());
        dto.put("sizeMax", fish.getSizeMax());
        dto.put("weightMin", fish.getWeightMin());
        dto.put("weightMax", fish.getWeightMax());
        dto.put("bestSeason", fish.getBestSeason());
        dto.put("bestTime", fish.getBestTime());
        dto.put("bestBait", fish.getBestBait());
        dto.put("fishingMethod", fish.getFishingMethod());
        dto.put("imageUrl", fish.getImageUrl());
        dto.put("rarity", fish.getRarity());
        dto.put("kingdom", fish.getKingdom());
        dto.put("phylum", fish.getPhylum());
        dto.put("className", fish.getClassName());
        dto.put("orderName", fish.getOrderName());
        dto.put("family", fish.getFamily());
        dto.put("createdAt", fish.getCreatedAt());
        
        // 생물분류-중 원본 텍스트
        dto.put("biologicalClassificationMiddle", fish.getBiologicalClassificationMiddle());
        
        // 생물분류 카테고리 정보 (SVG 아이콘 포함)
        if (fish.getBiologicalCategory() != null) {
            Map<String, Object> categoryInfo = new HashMap<>();
            categoryInfo.put("categoryId", fish.getBiologicalCategory().getCategoryId());
            categoryInfo.put("categoryName", fish.getBiologicalCategory().getCategoryName());
            categoryInfo.put("categoryType", fish.getBiologicalCategory().getCategoryType());
            categoryInfo.put("svgIcon", fish.getBiologicalCategory().getSvgIcon());
            categoryInfo.put("iconColor", fish.getBiologicalCategory().getIconColor());
            categoryInfo.put("backgroundGradient", fish.getBiologicalCategory().getBackgroundGradient());
            categoryInfo.put("description", fish.getBiologicalCategory().getDescription());
            
            dto.put("biologicalCategory", categoryInfo);
        }
        
        return dto;
    }
}