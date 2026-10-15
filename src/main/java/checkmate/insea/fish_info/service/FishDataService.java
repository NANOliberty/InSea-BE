package checkmate.insea.fish_info.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import checkmate.insea.fish_info.mapper.BiologicalCategoryRepository;
import checkmate.insea.fish_info.mapper.FishSpeciesRepository;
import checkmate.insea.fish_info.model.BiologicalCategory;
import checkmate.insea.fish_info.model.EnumClass.FishRarity;
import checkmate.insea.fish_info.model.FishSpecies;

@Service
public class FishDataService {
    
    private final FishSpeciesRepository fishSpeciesRepository;
    private final BiologicalCategoryRepository biologicalCategoryRepository;
    private final BiologicalCategoryService biologicalCategoryService;
    
    @Autowired
    public FishDataService(FishSpeciesRepository fishSpeciesRepository,
                          BiologicalCategoryRepository biologicalCategoryRepository,
                          BiologicalCategoryService biologicalCategoryService) {
        this.fishSpeciesRepository = fishSpeciesRepository;
        this.biologicalCategoryRepository = biologicalCategoryRepository;
        this.biologicalCategoryService = biologicalCategoryService;
    }
    
    /**
     * 엑셀 파일에서 어류 데이터를 읽어서 데이터베이스에 저장
     * (생물분류-중 정보와 SVG 아이콘 포함)
     */
    @Transactional
    public void importFishDataFromExcel(MultipartFile file) throws IOException {
        // SVG 데이터 초기화 (최초 1회)
        biologicalCategoryService.initializeSvgData();
        
        List<FishSpecies> fishSpeciesList = parseExcelToFishSpecies(file);
        
        System.out.println("총 " + fishSpeciesList.size() + "개의 데이터를 저장합니다.");
        
        // 배치로 저장
        fishSpeciesRepository.saveAll(fishSpeciesList);
        
        System.out.println("데이터 저장 완료");
    }
    
    /**
     * 엑셀 파일을 파싱하여 FishSpecies 리스트로 변환
     */
    private List<FishSpecies> parseExcelToFishSpecies(MultipartFile file) throws IOException {
        List<FishSpecies> fishSpeciesList = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // 헤더 행 건너뛰기
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // 생물분류-중 정보 추출
                String biologicalClassification = getCellValue(row, 5); // 생물분류-중 컬럼 (인덱스 5)
                
                // 모든 생물 데이터를 처리
                FishSpecies fishSpecies = createFishSpeciesFromRow(row, biologicalClassification);
                if (fishSpecies != null) {
                    fishSpeciesList.add(fishSpecies);
                }
            }
        }
        
        return fishSpeciesList;
    }
    
    /**
     * 엑셀 행 데이터를 FishSpecies 객체로 변환
     */
    private FishSpecies createFishSpeciesFromRow(Row row, String biologicalClassification) {
        try {
            String scientificName = getCellValue(row, 0);  // 학명
            String koreanName = getCellValue(row, 3);      // 국명
            String kingdom = getCellValue(row, 7);         // 계통분류-계-국명
            String phylum = getCellValue(row, 9);          // 계통분류-문-국명  
            String className = getCellValue(row, 11);      // 계통분류-강-국명
            String orderName = getCellValue(row, 13);      // 계통분류-목-국명
            String family = getCellValue(row, 15);         // 계통분류-과-국명
            
            // 필수 필드 검증
            if (koreanName == null || koreanName.trim().isEmpty()) {
                System.out.println("국명이 없는 데이터를 건너뜁니다: " + scientificName);
                return null;
            }
            
            // 생물분류-중에 해당하는 BiologicalCategory 찾기
            BiologicalCategory category = null;
            if (biologicalClassification != null && !biologicalClassification.trim().isEmpty()) {
                Optional<BiologicalCategory> categoryOpt = 
                    biologicalCategoryService.findByCategoryName(biologicalClassification.trim());
                
                if (categoryOpt.isPresent()) {
                    category = categoryOpt.get();
                } else {
                    // 새로운 카테고리 생성 (SVG 아이콘은 기본값으로)
                    category = createDefaultCategory(biologicalClassification.trim());
                    System.out.println("새로운 생물분류 카테고리 생성: " + biologicalClassification);
                }
            }
            
            FishSpecies fishSpecies = new FishSpecies();
            fishSpecies.setKoreanName(koreanName);
            fishSpecies.setScientificName(scientificName);
            fishSpecies.setBiologicalCategory(category);
            fishSpecies.setBiologicalClassificationMiddle(biologicalClassification);
            fishSpecies.setKingdom(kingdom);
            fishSpecies.setPhylum(phylum);
            fishSpecies.setClassName(className);
            fishSpecies.setOrderName(orderName);
            fishSpecies.setFamily(family);
            fishSpecies.setRarity(determineRarity(biologicalClassification));
            
            System.out.println("처리 중인 데이터: " + koreanName + ", 생물분류-중: " + biologicalClassification);
        
            return fishSpecies;
                    
        } catch (Exception e) {
            System.err.println("행 데이터 변환 중 오류 발생: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 기본 카테고리 생성 (SVG 아이콘이 정의되지 않은 새로운 분류용)
     */
    private BiologicalCategory createDefaultCategory(String categoryName) {
        BiologicalCategory category = new BiologicalCategory();
        category.setCategoryName(categoryName);
        category.setCategoryType("unknown");
        category.setSvgIcon(getDefaultSvgIcon());
        category.setIconColor("category-unknown");
        category.setBackgroundGradient("linear-gradient(135deg, #95a5a6, #7f8c8d)");
        category.setDescription("분류 정보가 추가로 필요한 생물");
        category.setDisplayOrder(999);
        category.setIsActive(true);
            
        return biologicalCategoryRepository.save(category);
    }
    
    /**
     * 기본 SVG 아이콘
     */
    private String getDefaultSvgIcon() {
        return "<circle cx=\"50\" cy=\"50\" r=\"20\" fill=\"white\"></circle>" +
               "<circle cx=\"45\" cy=\"45\" r=\"2\" fill=\"#2c3e50\"></circle>" +
               "<circle cx=\"55\" cy=\"45\" r=\"2\" fill=\"#2c3e50\"></circle>" +
               "<path d=\"M40 60 Q50 70, 60 60\" stroke=\"white\" stroke-width=\"2\" fill=\"none\"></path>";
    }
    
    /**
     * 생물분류에 따른 희귀도 결정
     */
    private FishRarity determineRarity(String biologicalClassification) {
        if (biologicalClassification == null) {
            return FishRarity.COMMON;
        }
        
        return switch (biologicalClassification.toLowerCase()) {
            case "어류" -> FishRarity.COMMON;
            case "세균", "균류" -> FishRarity.RARE;
            case "연체동물", "절지동물" -> FishRarity.UNCOMMON;
            case "포유류", "파충류" -> FishRarity.LEGENDARY;
            default -> FishRarity.COMMON;
        };
    }
    
    /**
     * 셀 값을 안전하게 문자열로 변환
     */
    private String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
    
    /**
     * 기존 데이터 삭제 후 새로운 데이터 임포트
     */
    @Transactional
    public void replaceAllFishData(MultipartFile file) throws IOException {
        System.out.println("기존 데이터를 모두 삭제합니다.");
        fishSpeciesRepository.deleteAll();
        
        System.out.println("새로운 데이터를 임포트합니다.");
        importFishDataFromExcel(file);
    }
    
    /**
     * 중복 데이터 확인 후 새로운 데이터만 추가
     */
    @Transactional
    public void importNewFishDataOnly(MultipartFile file) throws IOException {
        // SVG 데이터 초기화 (최초 1회)
        biologicalCategoryService.initializeSvgData();
        
        List<FishSpecies> fishSpeciesList = parseExcelToFishSpecies(file);
        List<FishSpecies> newFishSpeciesList = new ArrayList<>();
        
        for (FishSpecies fishSpecies : fishSpeciesList) {
            // 한국명으로 중복 체크
            boolean exists = fishSpeciesRepository.existsByKoreanName(fishSpecies.getKoreanName());
            if (!exists) {
                newFishSpeciesList.add(fishSpecies);
            }
        }
        
        System.out.println("총 " + fishSpeciesList.size() + "개 중 " + 
                         newFishSpeciesList.size() + "개의 새로운 데이터를 저장합니다.");
        
        if (!newFishSpeciesList.isEmpty()) {
            fishSpeciesRepository.saveAll(newFishSpeciesList);
        }
        
        System.out.println("새로운 데이터 저장 완료");
    }
    
    /**
     * 생물분류 카테고리별 통계 조회
     */
    public List<Object[]> getCategoryStatistics() {
        return fishSpeciesRepository.countByBiologicalCategory();
    }
    
    /**
     * 특정 생물분류의 모든 데이터 조회
     */
    public List<FishSpecies> getSpeciesByCategory(String categoryName) {
        return fishSpeciesRepository.findByBiologicalCategory_CategoryName(categoryName);
    }
    
    /**
     * 카테고리가 할당되지 않은 데이터 조회
     */
    public List<FishSpecies> getSpeciesWithoutCategory() {
        return fishSpeciesRepository.findByBiologicalCategoryIsNull();
    }
    
    /**
     * 생물분류 카테고리 재할당 (데이터 정리용)
     */
    @Transactional
    public void reassignCategories() {
        List<FishSpecies> speciesWithoutCategory = getSpeciesWithoutCategory();
        
        for (FishSpecies species : speciesWithoutCategory) {
            if (species.getBiologicalClassificationMiddle() != null) {
                Optional<BiologicalCategory> categoryOpt = 
                    biologicalCategoryService.findByCategoryName(species.getBiologicalClassificationMiddle().trim());
                
                if (categoryOpt.isPresent()) {
                    species.setBiologicalCategory(categoryOpt.get());
                    fishSpeciesRepository.save(species);
                    System.out.println("카테고리 재할당: " + species.getKoreanName() + 
                                     " -> " + categoryOpt.get().getCategoryName());
                }
            }
        }
    }
}