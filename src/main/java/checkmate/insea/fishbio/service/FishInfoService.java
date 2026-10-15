package checkmate.insea.fishbio.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import checkmate.insea.fishbio.dto.ApiResponse;
import checkmate.insea.fishbio.dto.FishInfoDto;
import checkmate.insea.fishbio.dto.MarineBioApiResponse;
import checkmate.insea.fishbio.entity.FishInfo;
import checkmate.insea.fishbio.repository.FishInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FishInfoService {
    
    private final FishInfoRepository fishInfoRepository;
    private final MarineBioApiService marineBioApiService;
    
    // 모든 해양생물 데이터를 API에서 가져와서 DB에 저장
    public ApiResponse<String> syncAllMarineBioData() {
        try {
            int numOfRows = 100;

            // 1. 첫 페이지를 호출하여 전체 데이터 수 확인
            MarineBioApiResponse initialResponse = marineBioApiService.fetchMarineBioData(1, 1);
            if (initialResponse == null || !"00".equals(initialResponse.getHeader().getResultCode())) {
                return ApiResponse.error("데이터 동기화 실패: API 초기 응답 오류");
            }
            int totalCount = initialResponse.getBody().getTotalCount();
            int totalPages = (totalCount + numOfRows - 1) / numOfRows;

            log.info("총 데이터: {}, 총 페이지: {}. 데이터 동기화를 시작합니다.", totalCount, totalPages);

            // 2. 각 페이지에 대한 비동기 작업을 생성하고 리스트에 담기
            List<CompletableFuture<Integer>> futures = IntStream.rangeClosed(1, totalPages)
                    .mapToObj(pageNo -> fetchAndSavePage(pageNo, numOfRows))
                    .collect(Collectors.toList());

            // 3. 모든 비동기 작업이 완료될 때까지 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 4. 모든 작업의 결과를 합산
            int totalSavedCount = futures.stream()
                    .map(CompletableFuture::join)
                    .mapToInt(Integer::intValue)
                    .sum();

            log.info("모든 페이지 동기화 완료. 총 {} 건 저장됨", totalSavedCount);
            return ApiResponse.success("데이터 동기화 완료: 총 " + totalSavedCount + "건 저장");

        } catch (Exception e) {
            log.error("데이터 동기화 중 오류 발생: {}", e.getMessage(), e);
            Thread.currentThread().interrupt(); // InterruptedException 처리
            return ApiResponse.error("데이터 동기화 실패: " + e.getMessage());
        }
    }

    /**
     * (신규 추가 및 @Async 적용)
     * 특정 페이지의 데이터를 가져와 DB에 저장하는 비동기 메서드.
     * 각 페이지 처리가 하나의 트랜잭션으로 묶입니다.
     */
    @Async
    @Transactional
    public CompletableFuture<Integer> fetchAndSavePage(int pageNo, int numOfRows) {
        log.info("페이지 {} 데이터 수집 시작...", pageNo);
        try {
            MarineBioApiResponse response = marineBioApiService.fetchMarineBioData(pageNo, numOfRows);

            if (response == null || !"00".equals(response.getHeader().getResultCode()) ||
                    response.getBody().getItems() == null || response.getBody().getItems().getItemList() == null) {
                log.warn("페이지 {} 데이터 수집 실패 또는 데이터 없음.", pageNo);
                return CompletableFuture.completedFuture(0);
            }

            int savedCount = 0;
            for (MarineBioApiResponse.Item item : response.getBody().getItems().getItemList()) {
                if (saveFishInfoFromApiItem(item)) {
                    savedCount++;
                }
            }

            log.info("페이지 {} 완료: {} 건 저장", pageNo, savedCount);
            Thread.sleep(100); // API 부하 방지를 위한 딜레이
            return CompletableFuture.completedFuture(savedCount);
        } catch (Exception e) {
            log.error("페이지 {} 처리 중 오류 발생: {}", pageNo, e.getMessage());
            Thread.currentThread().interrupt(); // InterruptedException 처리
            return CompletableFuture.completedFuture(0);
        }
    }
    
    private boolean saveFishInfoFromApiItem(MarineBioApiResponse.Item item) {
        try {
            // 필수 데이터 검증
            if (item.getScientificName() == null || item.getScientificName().trim().isEmpty()) {
                log.warn("학명이 없는 데이터 스킵: {}", item.getKoreanName());
                return false;
            }
            
            // 이미 존재하는 데이터인지 확인 (학명 기준)
            if (fishInfoRepository.existsByScientificName(item.getScientificName())) {
                log.debug("이미 존재하는 데이터: {}", item.getScientificName());
                return false;
            }
            
            FishInfo fishInfo = FishInfo.builder()
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
                    .scientificName(item.getScientificName().trim())
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
            
            // ID를 null로 명시적으로 설정 (자동 생성되도록)
            fishInfo.setId(null);
            
            FishInfo savedFishInfo = fishInfoRepository.save(fishInfo);
            log.debug("데이터 저장 완료: ID={}, 학명={}", savedFishInfo.getId(), savedFishInfo.getScientificName());
            return true;
            
        } catch (Exception e) {
            log.error("데이터 저장 중 오류 발생 - 학명: {}, 오류: {}", 
                     item.getScientificName(), e.getMessage(), e);
            return false;
        }
    }
    
    // 페이징된 물고기 목록 조회
    public ApiResponse<List<FishInfoDto>> getAllFishInfo(int page, int size, String sortBy) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
            Page<FishInfo> fishInfoPage = fishInfoRepository.findAll(pageable);
            
            List<FishInfoDto> fishInfoDtos = fishInfoPage.getContent().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fishInfoDtos, (int) fishInfoPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("물고기 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error("물고기 목록 조회 실패: " + e.getMessage());
        }
    }
    
    // 키워드로 물고기 검색
    public ApiResponse<List<FishInfoDto>> searchFishInfo(String keyword, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("koreanName").ascending());
            Page<FishInfo> fishInfoPage = fishInfoRepository.searchByKeyword(keyword, pageable);
            
            List<FishInfoDto> fishInfoDtos = fishInfoPage.getContent().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fishInfoDtos, (int) fishInfoPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("물고기 검색 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error("물고기 검색 실패: " + e.getMessage());
        }
    }
    
    // ID로 물고기 상세 정보 조회
    public ApiResponse<FishInfoDto> getFishInfoById(Long id) {
        try {
            Optional<FishInfo> fishInfo = fishInfoRepository.findById(id);
            
            if (fishInfo.isPresent()) {
                return ApiResponse.success(convertToDto(fishInfo.get()));
            } else {
                return ApiResponse.error("해당 ID의 물고기 정보를 찾을 수 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("물고기 상세 조회 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error("물고기 상세 조회 실패: " + e.getMessage());
        }
    }
    
    // 학명으로 물고기 정보 조회
    public ApiResponse<FishInfoDto> getFishInfoByScientificName(String scientificName) {
        try {
            Optional<FishInfo> fishInfo = fishInfoRepository.findByScientificName(scientificName);
            
            if (fishInfo.isPresent()) {
                return ApiResponse.success(convertToDto(fishInfo.get()));
            } else {
                return ApiResponse.error("해당 학명의 물고기 정보를 찾을 수 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("학명으로 물고기 조회 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error("학명으로 물고기 조회 실패: " + e.getMessage());
        }
    }
    
    // 한국명으로 물고기 검색
    public ApiResponse<List<FishInfoDto>> getFishInfoByKoreanName(String koreanName) {
        try {
            List<FishInfo> fishInfoList = fishInfoRepository.findByKoreanNameContaining(koreanName);
            
            List<FishInfoDto> fishInfoDtos = fishInfoList.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fishInfoDtos, fishInfoDtos.size());
            
        } catch (Exception e) {
            log.error("한국명으로 물고기 검색 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error("한국명으로 물고기 검색 실패: " + e.getMessage());
        }
    }
    
    // 과(Family)별 물고기 조회
    public ApiResponse<List<FishInfoDto>> getFishInfoByFamily(String familyKR) {
        try {
            List<FishInfo> fishInfoList = fishInfoRepository.findByFamilyKR(familyKR);
            
            List<FishInfoDto> fishInfoDtos = fishInfoList.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fishInfoDtos, fishInfoDtos.size());
            
        } catch (Exception e) {
            log.error("과별 물고기 조회 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error("과별 물고기 조회 실패: " + e.getMessage());
        }
    }

    // 서식지별 물고기 조회
    public ApiResponse<List<FishInfoDto>> getFishInfoByHabitat(String habitat) {
        try {
            List<FishInfo> fishInfoList = fishInfoRepository.findByHabitatContaining(habitat);
            
            List<FishInfoDto> fishInfoDtos = fishInfoList.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fishInfoDtos, fishInfoDtos.size());
            
        } catch (Exception e) {
            log.error("서식지별 물고기 조회 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error("서식지별 물고기 조회 실패: " + e.getMessage());
        }
    }
    
    // Entity를 DTO로 변환
    private FishInfoDto convertToDto(FishInfo fishInfo) {
        return FishInfoDto.builder()
                .id(fishInfo.getId())
                .kingdom(fishInfo.getKingdom())
                .kingdomKR(fishInfo.getKingdomKR())
                .phylumDivision(fishInfo.getPhylumDivision())
                .phylumDivisionKR(fishInfo.getPhylumDivisionKR())
                .className(fishInfo.getClassName())
                .classKR(fishInfo.getClassKR())
                .orderName(fishInfo.getOrderName())
                .orderKR(fishInfo.getOrderKR())
                .family(fishInfo.getFamily())
                .familyKR(fishInfo.getFamilyKR())
                .scientificName(fishInfo.getScientificName())
                .koreanName(fishInfo.getKoreanName())
                .speciesType(fishInfo.getSpeciesType())
                .abstractInfo(fishInfo.getAbstractInfo())
                .form(fishInfo.getForm())
                .ecology(fishInfo.getEcology())
                .cultivationInfo(fishInfo.getCultivationInfo())
                .biochemical(fishInfo.getBiochemical())
                .activeInfo(fishInfo.getActiveInfo())
                .nationalDistribution(fishInfo.getNationalDistribution())
                .internationalDistribution(fishInfo.getInternationalDistribution())
                .habitat(fishInfo.getHabitat())
                .utilization(fishInfo.getUtilization())
                .correctionNameType(fishInfo.getCorrectionNameType())
                .correctionScientificName(fishInfo.getCorrectionScientificName())
                .build();
    }
}
