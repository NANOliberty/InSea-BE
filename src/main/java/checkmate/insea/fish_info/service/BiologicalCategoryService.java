package checkmate.insea.fish_info.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import checkmate.insea.fish_info.mapper.BiologicalCategoryRepository;
import checkmate.insea.fish_info.model.BiologicalCategory;

@Service
public class BiologicalCategoryService {
    
    private final BiologicalCategoryRepository biologicalCategoryRepository;
    
    @Autowired
    public BiologicalCategoryService(BiologicalCategoryRepository biologicalCategoryRepository) {
        this.biologicalCategoryRepository = biologicalCategoryRepository;
    }
    
    /**
     * SVG 아이콘 데이터를 초기화
     */
    @Transactional
    public void initializeSvgData() {
        Map<String, CategoryData> svgData = createSvgData();
        
        int order = 1;
        for (Map.Entry<String, CategoryData> entry : svgData.entrySet()) {
            String categoryName = entry.getKey();
            CategoryData data = entry.getValue();
            
            if (!biologicalCategoryRepository.existsByCategoryName(categoryName)) {
                BiologicalCategory category = new BiologicalCategory(
                    categoryName,
                    data.type,
                    data.svgIcon,
                    data.iconColor,
                    data.backgroundGradient,
                    data.description,
                    true,
                    order++
                );
                    
                biologicalCategoryRepository.save(category);
                System.out.println("생물분류 카테고리 생성: " + categoryName);
            }
        }
    }
    
    /**
     * 카테고리명으로 조회
     */
    public Optional<BiologicalCategory> findByCategoryName(String categoryName) {
        return biologicalCategoryRepository.findByCategoryName(categoryName);
    }
    
    /**
     * 모든 활성화된 카테고리 조회
     */
    public List<BiologicalCategory> findAllActiveCategories() {
        return biologicalCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }
    
    /**
     * 카테고리별 어류 개수 통계
     */
    public List<Object[]> getCategoryStatistics() {
        return biologicalCategoryRepository.countFishByCategory();
    }
    
    private Map<String, CategoryData> createSvgData() {
        Map<String, CategoryData> svgData = new HashMap<>();
        
        // 어류
        svgData.put("어류", new CategoryData(
            "marine",
            "<path d=\"M20 50 C20 40, 30 35, 45 35 L70 35 C80 35, 85 40, 85 50 C85 60, 80 65, 70 65 L45 65 C30 65, 20 60, 20 50 Z\" fill=\"white\"></path>" +
            "<circle cx=\"65\" cy=\"45\" r=\"3\" fill=\"#2c3e50\"></circle>" +
            "<path d=\"M15 50 L25 45 L25 55 Z\" fill=\"white\"></path>" +
            "<path d=\"M75 40 L85 35 L85 45 Z\" fill=\"white\"></path>" +
            "<path d=\"M75 55 L85 65 L85 55 Z\" fill=\"white\"></path>",
            "category-marine",
            "linear-gradient(135deg, #3498db, #2980b9)",
            "바다와 민물에 서식하는 척추동물"
        ));
        
        // 세균
        svgData.put("세균", new CategoryData(
            "micro",
            "<circle cx=\"35\" cy=\"35\" r=\"8\" fill=\"white\" opacity=\"0.9\"></circle>" +
            "<circle cx=\"65\" cy=\"35\" r=\"6\" fill=\"white\" opacity=\"0.7\"></circle>" +
            "<circle cx=\"45\" cy=\"55\" r=\"10\" fill=\"white\" opacity=\"0.9\"></circle>" +
            "<circle cx=\"25\" cy=\"65\" r=\"5\" fill=\"white\" opacity=\"0.6\"></circle>" +
            "<circle cx=\"70\" cy=\"65\" r=\"7\" fill=\"white\" opacity=\"0.8\"></circle>" +
            "<circle cx=\"55\" cy=\"25\" r=\"4\" fill=\"white\" opacity=\"0.5\"></circle>",
            "category-micro",
            "linear-gradient(135deg, #e67e22, #d35400)",
            "단세포 미생물"
        ));
        
        // 연체동물
        svgData.put("연체동물", new CategoryData(
            "marine",
            "<path d=\"M30 40 C30 30, 40 25, 50 25 C60 25, 70 30, 70 40 L70 65 C70 70, 65 75, 50 75 C35 75, 30 70, 30 65 Z\" fill=\"white\"></path>" +
            "<path d=\"M35 45 C35 40, 40 38, 50 38 C60 38, 65 40, 65 45 L65 60 C65 62, 60 65, 50 65 C40 65, 35 62, 35 60 Z\" fill=\"#ecf0f1\"></path>" +
            "<circle cx=\"45\" cy=\"50\" r=\"2\" fill=\"#2c3e50\"></circle>" +
            "<circle cx=\"55\" cy=\"50\" r=\"2\" fill=\"#2c3e50\"></circle>",
            "category-marine",
            "linear-gradient(135deg, #9b59b6, #8e44ad)",
            "조개, 문어, 오징어 등 연체동물"
        ));
        
        // 절지동물
        svgData.put("절지동물", new CategoryData(
            "marine",
            "<ellipse cx=\"50\" cy=\"50\" rx=\"25\" ry=\"15\" fill=\"white\"></ellipse>" +
            "<path d=\"M25 45 L15 40 M25 50 L15 50 M25 55 L15 60\" stroke=\"white\" stroke-width=\"3\" fill=\"none\"></path>" +
            "<path d=\"M75 45 L85 40 M75 50 L85 50 M75 55 L85 60\" stroke=\"white\" stroke-width=\"3\" fill=\"none\"></path>" +
            "<circle cx=\"42\" cy=\"45\" r=\"2\" fill=\"#2c3e50\"></circle>" +
            "<circle cx=\"58\" cy=\"45\" r=\"2\" fill=\"#2c3e50\"></circle>" +
            "<path d=\"M45 38 L50 32 L55 38\" stroke=\"white\" stroke-width=\"2\" fill=\"none\"></path>",
            "category-marine",
            "linear-gradient(135deg, #e74c3c, #c0392b)",
            "게, 새우, 곤충 등 관절이 있는 동물"
        ));
        
        // 홍조류
        svgData.put("홍조류", new CategoryData(
            "plant",
            "<path d=\"M50 75 Q45 65, 40 55 Q35 45, 30 35 Q35 30, 40 35 Q45 45, 50 55\" fill=\"white\"></path>" +
            "<path d=\"M50 75 Q55 65, 60 55 Q65 45, 70 35 Q65 30, 60 35 Q55 45, 50 55\" fill=\"white\"></path>" +
            "<path d=\"M50 75 Q50 65, 50 55 Q50 45, 50 35 Q50 30, 50 35 Q50 45, 50 55\" fill=\"white\"></path>" +
            "<circle cx=\"50\" cy=\"80\" r=\"3\" fill=\"white\"></circle>",
            "category-plant",
            "linear-gradient(135deg, #27ae60, #229954)",
            "바다의 붉은 해조류"
        ));
        
        // 갈조류
        svgData.put("갈조류", new CategoryData(
            "plant",
            "<path d=\"M50 80 Q40 70, 35 60 Q30 50, 25 40 Q20 30, 25 25 Q30 30, 35 40\" fill=\"white\"></path>" +
            "<path d=\"M50 80 Q60 70, 65 60 Q70 50, 75 40 Q80 30, 75 25 Q70 30, 65 40\" fill=\"white\"></path>" +
            "<path d=\"M50 80 Q45 68, 45 55 Q45 42, 50 30 Q55 42, 55 55 Q55 68, 50 80\" fill=\"white\"></path>",
            "category-plant",
            "linear-gradient(135deg, #d68910, #b7950b)",
            "바다의 갈색 해조류"
        ));
        
        // 녹조류
        svgData.put("녹조류", new CategoryData(
            "plant",
            "<path d=\"M50 75 Q40 65, 38 50 Q36 35, 45 30 Q50 35, 52 50\" fill=\"white\"></path>" +
            "<path d=\"M50 75 Q60 65, 62 50 Q64 35, 55 30 Q50 35, 48 50\" fill=\"white\"></path>" +
            "<circle cx=\"45\" cy=\"45\" r=\"8\" fill=\"white\" opacity=\"0.7\"></circle>" +
            "<circle cx=\"55\" cy=\"45\" r=\"8\" fill=\"white\" opacity=\"0.7\"></circle>",
            "category-plant",
            "linear-gradient(135deg, #27ae60, #229954)",
            "바다의 녹색 해조류"
        ));
        
        // 포유류
        svgData.put("포유류", new CategoryData(
            "marine",
            "<path d=\"M20 55 C20 45, 30 35, 50 35 C70 35, 80 45, 80 55 C80 65, 70 70, 50 70 C30 70, 20 65, 20 55 Z\" fill=\"white\"></path>" +
            "<path d=\"M15 50 Q10 45, 15 45 Q20 50, 15 55 Q10 55, 15 50\" fill=\"white\"></path>" +
            "<circle cx=\"40\" cy=\"50\" r=\"2\" fill=\"#2c3e50\"></circle>" +
            "<path d=\"M75 45 Q85 40, 85 50 Q75 55, 75 45\" fill=\"white\"></path>",
            "category-marine",
            "linear-gradient(135deg, #34495e, #2c3e50)",
            "바다의 포유동물"
        ));
        
        // 자포동물
        svgData.put("자포동물", new CategoryData(
            "marine",
            "<circle cx=\"50\" cy=\"40\" r=\"15\" fill=\"white\"></circle>" +
            "<path d=\"M40 55 Q45 65, 50 70 Q55 65, 60 55\" fill=\"white\"></path>" +
            "<path d=\"M35 58 Q40 68, 45 72\" stroke=\"white\" stroke-width=\"2\" fill=\"none\"></path>" +
            "<path d=\"M65 58 Q60 68, 55 72\" stroke=\"white\" stroke-width=\"2\" fill=\"none\"></path>" +
            "<path d=\"M30 60 Q35 70, 40 74\" stroke=\"white\" stroke-width=\"2\" fill=\"none\"></path>" +
            "<path d=\"M70 60 Q65 70, 60 74\" stroke=\"white\" stroke-width=\"2\" fill=\"none\"></path>",
            "category-marine",
            "linear-gradient(135deg, #f39c12, #e67e22)",
            "해파리, 산호류"
        ));
        
        // 균류
        svgData.put("균류", new CategoryData(
            "micro",
            "<path d=\"M45 70 L45 50 Q45 40, 50 35 Q55 40, 55 50 L55 70\" fill=\"white\"></path>" +
            "<path d=\"M35 50 Q35 35, 50 35 Q65 35, 65 50 Q65 55, 60 55 L40 55 Q35 55, 35 50\" fill=\"white\"></path>" +
            "<ellipse cx=\"50\" cy=\"75\" rx=\"8\" ry=\"3\" fill=\"white\"></ellipse>",
            "category-micro",
            "linear-gradient(135deg, #8e44ad, #9b59b6)",
            "버섯, 곰팡이류"
        ));
        
        // 극피동물
        svgData.put("극피동물", new CategoryData(
            "marine",
            "<circle cx=\"50\" cy=\"50\" r=\"18\" fill=\"white\"></circle>" +
            "<path d=\"M50 32 L50 25 M32 50 L25 50 M68 50 L75 50 M50 68 L50 75\" stroke=\"white\" stroke-width=\"3\"></path>" +
            "<path d=\"M61 39 L66 34 M39 61 L34 66 M61 61 L66 66 M39 39 L34 34\" stroke=\"white\" stroke-width=\"2\"></path>" +
            "<circle cx=\"50\" cy=\"50\" r=\"5\" fill=\"#ecf0f1\"></circle>",
            "category-marine",
            "linear-gradient(135deg, #e74c3c, #c0392b)",
            "불가사리, 성게류"
        ));
        
        // 편모조류
        svgData.put("편모조류", new CategoryData(
            "plant",
            "<circle cx=\"50\" cy=\"50\" r=\"12\" fill=\"white\"></circle>" +
            "<path d=\"M50 38 Q45 25, 35 30 Q40 35, 48 40\" stroke=\"white\" stroke-width=\"2\" fill=\"none\"></path>" +
            "<path d=\"M62 50 Q75 45, 70 35 Q65 40, 58 48\" stroke=\"white\" stroke-width=\"2\" fill=\"none\"></path>" +
            "<circle cx=\"48\" cy=\"48\" r=\"1.5\" fill=\"#27ae60\"></circle>" +
            "<circle cx=\"52\" cy=\"52\" r=\"1.5\" fill=\"#27ae60\"></circle>",
            "category-plant",
            "linear-gradient(135deg, #27ae60, #229954)",
            "편모를 가진 조류"
        ));
        
        // 해면동물
        svgData.put("해면동물", new CategoryData(
            "marine",
            "<path d=\"M35 75 Q35 65, 40 60 Q45 55, 50 50 Q55 55, 60 60 Q65 65, 65 75\" fill=\"white\"></path>" +
            "<circle cx=\"42\" cy=\"65\" r=\"3\" fill=\"none\" stroke=\"white\" stroke-width=\"2\"></circle>" +
            "<circle cx=\"58\" cy=\"65\" r=\"3\" fill=\"none\" stroke=\"white\" stroke-width=\"2\"></circle>" +
            "<circle cx=\"50\" cy=\"58\" r=\"2\" fill=\"none\" stroke=\"white\" stroke-width=\"2\"></circle>" +
            "<path d=\"M45 45 L45 40 M55 45 L55 40 M50 35 L50 30\" stroke=\"white\" stroke-width=\"2\"></path>",
            "category-marine",
            "linear-gradient(135deg, #3498db, #2980b9)",
            "해면류"
        ));
        
        // 규조류
        svgData.put("규조류", new CategoryData(
            "plant",
            "<circle cx=\"50\" cy=\"50\" r=\"20\" fill=\"white\"></circle>" +
            "<circle cx=\"50\" cy=\"50\" r=\"15\" fill=\"none\" stroke=\"#27ae60\" stroke-width=\"2\"></circle>" +
            "<circle cx=\"50\" cy=\"50\" r=\"10\" fill=\"none\" stroke=\"#27ae60\" stroke-width=\"1\"></circle>" +
            "<circle cx=\"50\" cy=\"50\" r=\"5\" fill=\"#27ae60\"></circle>" +
            "<path d=\"M30 50 L25 50 M70 50 L75 50 M50 30 L50 25 M50 70 L50 75\" stroke=\"white\" stroke-width=\"2\"></path>",
            "category-plant",
            "linear-gradient(135deg, #27ae60, #229954)",
            "규조류"
        ));
        
        // 파충류
        svgData.put("파충류", new CategoryData(
            "marine",
            "<ellipse cx=\"50\" cy=\"45\" rx=\"20\" ry=\"12\" fill=\"white\"></ellipse>" +
            "<ellipse cx=\"45\" cy=\"55\" rx=\"15\" ry=\"8\" fill=\"white\"></ellipse>" +
            "<path d=\"M65 40 Q75 35, 80 40 Q75 45, 70 45\" fill=\"white\"></path>" +
            "<path d=\"M30 55 Q20 50, 15 55 Q20 60, 25 60\" fill=\"white\"></path>" +
            "<path d=\"M35 65 Q25 70, 30 75 Q35 70, 35 65\" fill=\"white\"></path>" +
            "<path d=\"M65 65 Q75 70, 70 75 Q65 70, 65 65\" fill=\"white\"></path>" +
            "<circle cx=\"55\" cy=\"42\" r=\"2\" fill=\"#2c3e50\"></circle>",
            "category-marine",
            "linear-gradient(135deg, #16a085, #27ae60)",
            "바다뱀 등 파충류"
        ));
        
        // 관속식물
        svgData.put("관속식물", new CategoryData(
            "plant",
            "<path d=\"M50 75 L50 45\" stroke=\"white\" stroke-width=\"4\"></path>" +
            "<path d=\"M40 55 Q35 50, 30 45 Q35 40, 40 45 Q45 50, 50 55\" fill=\"white\"></path>" +
            "<path d=\"M60 55 Q65 50, 70 45 Q65 40, 60 45 Q55 50, 50 55\" fill=\"white\"></path>" +
            "<path d=\"M50 45 Q45 40, 40 35 Q45 30, 50 35 Q55 30, 60 35 Q55 40, 50 45\" fill=\"white\"></path>",
            "category-plant",
            "linear-gradient(135deg, #27ae60, #229954)",
            "관다발이 있는 식물"
        ));
        
        // 와편모조류
        svgData.put("와편모조류", new CategoryData(
            "plant",
            "<circle cx=\"50\" cy=\"50\" r=\"15\" fill=\"white\"></circle>" +
            "<path d=\"M35 50 Q25 40, 30 30 Q40 35, 45 45\" stroke=\"white\" stroke-width=\"2\" fill=\"none\"></path>" +
            "<path d=\"M35 50 Q25 60, 30 70 Q40 65, 45 55\" stroke=\"white\" stroke-width=\"2\" fill=\"none\"></path>" +
            "<circle cx=\"50\" cy=\"50\" r=\"8\" fill=\"#e74c3c\" opacity=\"0.7\"></circle>",
            "category-plant",
            "linear-gradient(135deg, #e74c3c, #c0392b)",
            "와편모조류"
        ));
        
        return svgData;
    }
    
    /**
     * SVG 데이터를 담는 내부 클래스
     */
    private static class CategoryData {
        String type;
        String svgIcon;
        String iconColor;
        String backgroundGradient;
        String description;
        
        CategoryData(String type, String svgIcon, String iconColor, String backgroundGradient, String description) {
            this.type = type;
            this.svgIcon = svgIcon;
            this.iconColor = iconColor;
            this.backgroundGradient = backgroundGradient;
            this.description = description;
        }
    }
}