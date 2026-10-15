// package checkmate.insea.fishbio.service;

// import java.util.List;

// import checkmate.insea.fishbio.dto.ApiResponse;
// import checkmate.insea.fishbio.dto.FishInfoDto;
// import checkmate.insea.fishbio.entity.FishInfo;

// public class FishInfoServiceExtended {
    
//     // 과(Family)별 물고기 조회
//     public ApiResponse<List<FishInfoDto>> getFishInfoByFamily(String familyKR) {
//         try {
//             List<FishInfo> fishInfoList = fishInfoRepository.findByFamilyKR(familyKR);
            
//             List<FishInfoDto> fishInfoDtos = fishInfoList.stream()
//                     .map(this::convertToDto)
//                     .collect(Collectors.toList());
            
//             return ApiResponse.success(fishInfoDtos, fishInfoDtos.size());
            
//         } catch (Exception e) {
//             log.error("과별 물고기 조회 중 오류 발생: {}", e.getMessage(), e);
//             return ApiResponse.error("과별 물고기 조회 실패: " + e.getMessage());
//         }
//     }
    
//     // 서식지별 물고기 조회
//     public ApiResponse<List<FishInfoDto>> getFishInfoByHabitat(String habitat) {
//         try {
//             List<FishInfo> fishInfoList = fishInfoRepository.findByHabitatContaining(habitat);
            
//             List<FishInfoDto> fishInfoDtos = fishInfoList.stream()
//                     .map(this::convertToDto)
//                     .collect(Collectors.toList());
            
//             return ApiResponse.success(fishInfoDtos, fishInfoDtos.size());
            
//         } catch (Exception e) {
//             log.error("서식지별 물고기 조회 중 오류 발생: {}", e.getMessage(), e);
//             return ApiResponse.error("서식지별 물고기 조회 실패: " + e.getMessage());
//         }
//     }
    
//     // 통계 정보 조회
//     public ApiResponse<Map<String, Object>> getFishStatistics() {
//         try {
//             long totalCount = fishInfoRepository.count();
            
//             // 과별 통계 (상위 10개)
//             List<Object[]> familyStats = fishInfoRepository.findTopFamiliesByCount();
            
//             // 서식지별 통계
//             List<Object[]> habitatStats = fishInfoRepository.findHabitatStatistics();
            
//             Map<String, Object> statistics = new HashMap<>();
//             statistics.put("totalCount", totalCount);
//             statistics.put("familyStats", familyStats);
//             statistics.put("habitatStats", habitatStats);
            
//             return ApiResponse.success(statistics);
            
//         } catch (Exception e) {
//             log.error("통계 정보 조회 중 오류 발생: {}", e.getMessage(), e);
//             return ApiResponse.error("통계 정보 조회 실패: " + e.getMessage());
//         }
//     }
// }