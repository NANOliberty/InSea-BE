package checkmate.insea.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import checkmate.insea.map.model.Place;
import checkmate.insea.map.repository.PlaceRepository;
import checkmate.insea.user.dto.favorite.FavoriteDto;
import checkmate.insea.user.model.User;
import checkmate.insea.user.model.UserFavorite;
import checkmate.insea.user.repository.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {
    
    private final UserFavoriteRepository userFavoriteRepository;
    private final PlaceRepository placeRepository;
    
    /**
     * 즐겨찾기 추가/제거 토글
     */
    @Transactional
    public FavoriteDto.ToggleResponse toggleFavorite(User user, Long placeId) {
        // 장소 존재 확인
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 장소입니다."));
        
        // 기존 즐겨찾기 확인
        Optional<UserFavorite> existingFavorite = userFavoriteRepository.findByUserIdAndPlaceId(user.getUserId(), placeId);
        
        if (existingFavorite.isPresent()) {
            // 즐겨찾기 제거
            userFavoriteRepository.delete(existingFavorite.get());
            log.info("사용자 {}가 장소 {}를 즐겨찾기에서 제거했습니다.", user.getUserId(), placeId);
            
            return FavoriteDto.ToggleResponse.builder()
                    .isFavorite(false)
                    .message("즐겨찾기에서 제거되었습니다.")
                    .build();
        } else {
            // 즐겨찾기 추가
            UserFavorite favorite = UserFavorite.builder()
                    .user(user)
                    .place(place)
                    .build();
            
            UserFavorite saved = userFavoriteRepository.save(favorite);
            log.info("사용자 {}가 장소 {}를 즐겨찾기에 추가했습니다.", user.getUserId(), placeId);
            
            return FavoriteDto.ToggleResponse.builder()
                    .isFavorite(true)
                    .message("즐겨찾기에 추가되었습니다.")
                    .favoriteId(saved.getId())
                    .build();
        }
    }
    
    /**
     * 사용자의 즐겨찾기 목록 조회 (전체)
     */
    public FavoriteDto.FavoriteListResponse getFavorites(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserFavorite> favoritePage = userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId(), pageable);
        
        List<FavoriteDto.FavoriteInfo> favorites = favoritePage.getContent().stream()
                .map(FavoriteDto.FavoriteInfo::from)
                .toList();
        
        return FavoriteDto.FavoriteListResponse.builder()
                .favorites(favorites)
                .currentPage(favoritePage.getNumber())
                .totalPages(favoritePage.getTotalPages())
                .totalElements(favoritePage.getTotalElements())
                .hasNext(favoritePage.hasNext())
                .hasPrevious(favoritePage.hasPrevious())
                .build();
    }
    
    /**
     * 사용자의 특정 카테고리 즐겨찾기 목록 조회
     */
    public FavoriteDto.FavoriteListResponse getFavoritesByContentType(User user, Integer contentTypeId, int page, int size) {
        // contentTypeId 유효성 검사
        if (!isValidContentTypeId(contentTypeId)) {
            throw new IllegalArgumentException("유효하지 않은 컨텐츠 타입입니다. (28: 레포츠, 32: 숙박, 39: 음식점)");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserFavorite> favoritePage = userFavoriteRepository.findByUserIdAndContentTypeIdOrderByCreatedAtDesc(
                user.getUserId(), contentTypeId, pageable);
        
        List<FavoriteDto.FavoriteInfo> favorites = favoritePage.getContent().stream()
                .map(FavoriteDto.FavoriteInfo::from)
                .toList();
        
        return FavoriteDto.FavoriteListResponse.builder()
                .favorites(favorites)
                .currentPage(favoritePage.getNumber())
                .totalPages(favoritePage.getTotalPages())
                .totalElements(favoritePage.getTotalElements())
                .hasNext(favoritePage.hasNext())
                .hasPrevious(favoritePage.hasPrevious())
                .build();
    }
    
    /**
     * 즐겨찾기 통계 조회
     */
    public FavoriteDto.FavoriteListResponse.Summary getFavoritesSummary(User user) {
        long totalFavorites = userFavoriteRepository.countByUserUserId(user.getUserId());
        long sportsCount = userFavoriteRepository.countByUserIdAndContentTypeId(user.getUserId(), 28);
        long accommodationCount = userFavoriteRepository.countByUserIdAndContentTypeId(user.getUserId(), 32);
        long restaurantCount = userFavoriteRepository.countByUserIdAndContentTypeId(user.getUserId(), 39);
        
        return FavoriteDto.FavoriteListResponse.Summary.builder()
                .totalFavorites(totalFavorites)
                .sportsCount(sportsCount)
                .accommodationCount(accommodationCount)
                .restaurantCount(restaurantCount)
                .build();
    }
    
    /**
     * 특정 장소의 즐겨찾기 여부 확인
     */
    public boolean isFavorite(User user, Long placeId) {
        return userFavoriteRepository.existsByUserUserIdAndPlaceId(user.getUserId(), placeId);
    }
    
    /**
     * 즐겨찾기 제거 (ID로)
     */
    @Transactional
    public void removeFavorite(User user, Long favoriteId) {
        UserFavorite favorite = userFavoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 즐겨찾기입니다."));
        
        // 소유자 확인
        if (!favorite.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("해당 즐겨찾기를 삭제할 권한이 없습니다.");
        }
        
        userFavoriteRepository.delete(favorite);
        log.info("사용자 {}가 즐겨찾기 {}를 제거했습니다.", user.getUserId(), favoriteId);
    }
    
    private boolean isValidContentTypeId(Integer contentTypeId) {
        return contentTypeId != null && (contentTypeId == 28 || contentTypeId == 32 || contentTypeId == 39);
    }
}