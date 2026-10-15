package checkmate.insea.fishbio.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import checkmate.insea.fishbio.dto.ApiResponse;
import checkmate.insea.fishbio.dto.FishInfoDto;
import checkmate.insea.fishbio.dto.MarineBioApiResponse;
import checkmate.insea.fishbio.service.FishInfoService;
import checkmate.insea.fishbio.service.MarineBioApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/fish")
@RequiredArgsConstructor
@Slf4j
public class FishInfoController {
    
    private final FishInfoService fishInfoService;
    private final MarineBioApiService marineBioApiService;
    
    // 데이터 동기화 (초기 데이터 로딩)
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncMarineBioData() {
        log.info("해양생물 데이터 동기화 요청");
        ApiResponse<String> response = fishInfoService.syncAllMarineBioData();
        return ResponseEntity.ok(response);
    }
    
    // 전체 물고기 목록 조회 (페이징)
    @GetMapping
    public ResponseEntity<ApiResponse<List<FishInfoDto>>> getAllFishInfo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "koreanName") String sortBy) {
        
        log.info("물고기 목록 조회 요청 - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        ApiResponse<List<FishInfoDto>> response = fishInfoService.getAllFishInfo(page, size, sortBy);
        return ResponseEntity.ok(response);
    }
    
    // 키워드 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FishInfoDto>>> searchFishInfo(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("물고기 검색 요청 - keyword: {}, page: {}, size: {}", keyword, page, size);
        ApiResponse<List<FishInfoDto>> response = fishInfoService.searchFishInfo(keyword, page, size);
        return ResponseEntity.ok(response);
    }
    
    // ID로 물고기 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FishInfoDto>> getFishInfoById(@PathVariable Long id) {
        log.info("물고기 상세 조회 요청 - id: {}", id);
        ApiResponse<FishInfoDto> response = fishInfoService.getFishInfoById(id);
        return ResponseEntity.ok(response);
    }
    
    // 학명으로 물고기 조회
    @GetMapping("/scientific/{scientificName}")
    public ResponseEntity<ApiResponse<FishInfoDto>> getFishInfoByScientificName(
            @PathVariable String scientificName) {
        
        log.info("학명으로 물고기 조회 요청 - scientificName: {}", scientificName);
        ApiResponse<FishInfoDto> response = fishInfoService.getFishInfoByScientificName(scientificName);
        return ResponseEntity.ok(response);
    }
    
    // 한국명으로 물고기 검색
    @GetMapping("/korean/{koreanName}")
    public ResponseEntity<ApiResponse<List<FishInfoDto>>> getFishInfoByKoreanName(
            @PathVariable String koreanName) {
        
        log.info("한국명으로 물고기 검색 요청 - koreanName: {}", koreanName);
        ApiResponse<List<FishInfoDto>> response = fishInfoService.getFishInfoByKoreanName(koreanName);
        return ResponseEntity.ok(response);
    }
    
    // 과(Family)별 물고기 조회
    @GetMapping("/family/{familyKR}")
    public ResponseEntity<ApiResponse<List<FishInfoDto>>> getFishInfoByFamily(
            @PathVariable String familyKR) {
        
        log.info("과별 물고기 조회 요청 - familyKR: {}", familyKR);
        ApiResponse<List<FishInfoDto>> response = fishInfoService.getFishInfoByFamily(familyKR);
        return ResponseEntity.ok(response);
    }
    
    // 서식지별 물고기 조회
    @GetMapping("/habitat/{habitat}")
    public ResponseEntity<ApiResponse<List<FishInfoDto>>> getFishInfoByHabitat(
            @PathVariable String habitat) {
        
        log.info("서식지별 물고기 조회 요청 - habitat: {}", habitat);
        ApiResponse<List<FishInfoDto>> response = fishInfoService.getFishInfoByHabitat(habitat);
        return ResponseEntity.ok(response);
    }
    
    // // 통계 정보 조회
    // @GetMapping("/statistics")
    // public ResponseEntity<ApiResponse<Map<String, Object>>> getFishStatistics() {
    //     log.info("물고기 통계 정보 조회 요청");
    //     ApiResponse<Map<String, Object>> response = fishInfoService.getFishStatistics();
    //     return ResponseEntity.ok(response);
    // }


        // 외부 API 전체 목록 조회
    @GetMapping("/external/list")
    public ResponseEntity<ApiResponse<List<FishInfoDto>>> getExternalApiList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int numOfRows) {
        
        log.info("외부 API 전체 목록 조회 - pageNo: {}, numOfRows: {}", pageNo, numOfRows);
        
        try {
            MarineBioApiResponse response = marineBioApiService.fetchMarineBioData(pageNo, numOfRows);
            
            if (response == null || !"00".equals(response.getHeader().getResultCode())) {
                return ResponseEntity.ok(ApiResponse.error("외부 API 호출 실패"));
            }
            
            List<FishInfoDto> fishInfoDtos = new ArrayList<>();
            if (response.getBody() != null && 
                response.getBody().getItems() != null && 
                response.getBody().getItems().getItemList() != null) {
                
                fishInfoDtos = response.getBody().getItems().getItemList().stream()
                        .filter(item -> item.getScientificName() != null && !item.getScientificName().isEmpty())
                        .filter(item -> !"Bacteria".equals(item.getKingdom()) && 
                                       !"Actinomycetes".equals(item.getClassName()))
                        .map(this::convertApiItemToDto)
                        .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(ApiResponse.success(fishInfoDtos, response.getBody().getTotalCount()));
            
        } catch (Exception e) {
            log.error("외부 API 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("외부 API 목록 조회 실패: " + e.getMessage()));
        }
    }
    
    // MarineBioApiResponse.Item을 FishInfoDto로 변환
    private FishInfoDto convertApiItemToDto(MarineBioApiResponse.Item item) {
        return FishInfoDto.builder()
                .id(null) // 외부 API 데이터는 우리 DB의 ID가 없음
                .kingdom(item.getKingdom())
                .kingdomKR(item.getKingdomKR())
                .phylumDivision(item.getPhylumDivision())
                .phylumDivisionKR(item.getPhylumDivisionKR())
                .className(item.getClassName())
                .classKR(item.getClassKR())
                .orderName(item.getOrderName())
                .orderKR(item.getOrderKR())
                .family(item.getFamily())
                .familyKR(item.getFamilyKR())
                .scientificName(item.getScientificName())
                .koreanName(item.getKoreanName())
                .speciesType(item.getSpeciesType())
                .abstractInfo(item.getAbstractInfo())
                .form(item.getForm())
                .ecology(item.getEcology())
                .cultivationInfo(item.getCultivationInfo())
                .biochemical(item.getBiochemical())
                .activeInfo(item.getActiveInfo())
                .nationalDistribution(item.getNationalDistribution())
                .internationalDistribution(item.getInternationalDistribution())
                .habitat(item.getHabitat())
                .utilization(item.getUtilization())
                .correctionNameType(item.getCorrectionNameType())
                .correctionScientificName(item.getCorrectionScientificName())
                .build();
    }


    // 외부 API 직접 검색 (DB 거치지 않음)
    @GetMapping("/external/search")
    public ResponseEntity<ApiResponse<List<FishInfoDto>>> searchExternalApi(
            @RequestParam(required = false) String scientificName,
            @RequestParam(required = false) String koreanName,
            @RequestParam(required = false) String family,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int numOfRows) {
        
        log.info("외부 API 직접 검색 - scientificName: {}, koreanName: {}, family: {}", 
                scientificName, koreanName, family);
        
        try {
            MarineBioApiResponse response = marineBioApiService.searchMarineBioData(
                    scientificName, koreanName, family, pageNo, numOfRows);
            
            if (response == null || !"00".equals(response.getHeader().getResultCode())) {
                return ResponseEntity.ok(ApiResponse.error("외부 API 호출 실패"));
            }
            
            List<FishInfoDto> fishInfoDtos = new ArrayList<>();
            if (response.getBody() != null && 
                response.getBody().getItems() != null && 
                response.getBody().getItems().getItemList() != null) {
                
                fishInfoDtos = response.getBody().getItems().getItemList().stream()
                        .filter(item -> item.getScientificName() != null && !item.getScientificName().isEmpty())
                        .filter(item -> !"Bacteria".equals(item.getKingdom()) && 
                                    !"Actinomycetes".equals(item.getClassName()))
                        .map(this::convertApiItemToDto)
                        .map(this::fixKoreanEncoding) // 한글 인코딩 수정
                        .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(ApiResponse.success(fishInfoDtos, response.getBody().getTotalCount()));
            
        } catch (Exception e) {
            log.error("외부 API 검색 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("외부 API 검색 실패: " + e.getMessage()));
        }
    }

    // 한글 인코딩 수정 헬퍼 메서드
    private FishInfoDto fixKoreanEncoding(FishInfoDto dto) {
        try {
            if (dto.getKoreanName() != null) {
                dto.setKoreanName(fixString(dto.getKoreanName()));
            }
            if (dto.getKingdomKR() != null) {
                dto.setKingdomKR(fixString(dto.getKingdomKR()));
            }
            if (dto.getPhylumDivisionKR() != null) {
                dto.setPhylumDivisionKR(fixString(dto.getPhylumDivisionKR()));
            }
            if (dto.getClassKR() != null) {
                dto.setClassKR(fixString(dto.getClassKR()));
            }
            if (dto.getOrderKR() != null) {
                dto.setOrderKR(fixString(dto.getOrderKR()));
            }
            if (dto.getFamilyKR() != null) {
                dto.setFamilyKR(fixString(dto.getFamilyKR()));
            }
            // 다른 한글 필드들도 필요하면 추가
            return dto;
        } catch (Exception e) {
            log.warn("한글 인코딩 수정 실패: {}", e.getMessage());
            return dto;
        }
    }

    private String fixString(String str) {
        try {
            if (str == null || str.isEmpty()) return str;
            byte[] bytes = str.getBytes("ISO-8859-1");
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            return str; // 실패하면 원본 반환
        }
    }
}