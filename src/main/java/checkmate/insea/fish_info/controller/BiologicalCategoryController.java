package checkmate.insea.fish_info.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import checkmate.insea.fish_info.model.BiologicalCategory;
import checkmate.insea.fish_info.service.BiologicalCategoryService;

@RestController
@RequestMapping("/api/biological-categories")
@CrossOrigin(origins = "*")
public class BiologicalCategoryController {

    private final BiologicalCategoryService biologicalCategoryService;

    @Autowired
    public BiologicalCategoryController(BiologicalCategoryService biologicalCategoryService) {
        this.biologicalCategoryService = biologicalCategoryService;
    }

    /**
     * 모든 활성화된 생물분류 카테고리 조회 (SVG 아이콘 포함)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        try {
            List<BiologicalCategory> categories = biologicalCategoryService.findAllActiveCategories();
            
            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories);
            response.put("count", categories.size());
            response.put("message", "생물분류 카테고리 목록 조회 성공");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("생물분류 카테고리 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(
                Map.of("error", "카테고리 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 특정 카테고리 상세 조회
     */
    @GetMapping("/{categoryName}")
    public ResponseEntity<Map<String, Object>> getCategoryByName(@PathVariable String categoryName) {
        try {
            Optional<BiologicalCategory> category = biologicalCategoryService.findByCategoryName(categoryName);
            
            if (category.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "category", category.get(),
                    "message", "카테고리 조회 성공"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("카테고리 상세 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(
                Map.of("error", "카테고리 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * SVG 아이콘 데이터 초기화 (관리자용)
     */
    @PostMapping("/initialize-svg")
    public ResponseEntity<Map<String, Object>> initializeSvgData() {
        try {
            biologicalCategoryService.initializeSvgData();
            
            return ResponseEntity.ok(Map.of(
                "message", "SVG 아이콘 데이터 초기화가 완료되었습니다.",
                "status", "success"
            ));
        } catch (Exception e) {
            System.err.println("SVG 데이터 초기화 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(
                Map.of("error", "SVG 데이터 초기화 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 카테고리별 어류 개수 통계
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCategoryStatistics() {
        try {
            List<Object[]> stats = biologicalCategoryService.getCategoryStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("statistics", stats.stream().map(stat -> 
                Map.of(
                    "categoryName", stat[0],
                    "fishCount", stat[1]
                )
            ).toList());
            response.put("totalCategories", stats.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("카테고리 통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(
                Map.of("error", "통계 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * SVG 아이콘만 조회 (프론트엔드에서 아이콘 표시용)
     */
    @GetMapping("/icons")
    public ResponseEntity<Map<String, Object>> getAllIcons() {
        try {
            List<BiologicalCategory> categories = biologicalCategoryService.findAllActiveCategories();
            
            Map<String, Map<String, Object>> icons = new HashMap<>();
            for (BiologicalCategory category : categories) {
                Map<String, Object> iconData = new HashMap<>();
                iconData.put("svgIcon", category.getSvgIcon());
                iconData.put("iconColor", category.getIconColor());
                iconData.put("backgroundGradient", category.getBackgroundGradient());
                iconData.put("categoryType", category.getCategoryType());
                
                icons.put(category.getCategoryName(), iconData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("icons", icons);
            response.put("count", icons.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("아이콘 데이터 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(
                Map.of("error", "아이콘 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 특정 카테고리의 SVG 아이콘 조회
     */
    @GetMapping("/{categoryName}/icon")
    public ResponseEntity<Map<String, Object>> getCategoryIcon(@PathVariable String categoryName) {
        try {
            Optional<BiologicalCategory> category = biologicalCategoryService.findByCategoryName(categoryName);
            
            if (category.isPresent()) {
                BiologicalCategory cat = category.get();
                Map<String, Object> iconData = new HashMap<>();
                iconData.put("categoryName", cat.getCategoryName());
                iconData.put("svgIcon", cat.getSvgIcon());
                iconData.put("iconColor", cat.getIconColor());
                iconData.put("backgroundGradient", cat.getBackgroundGradient());
                iconData.put("categoryType", cat.getCategoryType());
                
                return ResponseEntity.ok(Map.of(
                    "icon", iconData,
                    "message", "아이콘 조회 성공"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("카테고리 아이콘 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(
                Map.of("error", "아이콘 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }
}