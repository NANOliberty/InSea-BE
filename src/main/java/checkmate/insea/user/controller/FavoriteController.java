package checkmate.insea.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import checkmate.insea.user.dto.favorite.FavoriteDto;
import checkmate.insea.user.model.User;
import checkmate.insea.user.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@CrossOrigin(origins="*", maxAge = 3600)
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    
    /**
     * 즐겨찾기 추가/제거 토글
     */
    @PostMapping("/toggle/{placeId}")
    public ResponseEntity<FavoriteDto.ToggleResponse> toggleFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Long placeId) {
        
        log.info("즐겨찾기 토글 요청 - 사용자: {}, 장소: {}", user.getUserId(), placeId);
        
        FavoriteDto.ToggleResponse response = favoriteService.toggleFavorite(user, placeId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 내 즐겨찾기 목록 조회 (전체)
     */
    @GetMapping
    public ResponseEntity<FavoriteDto.FavoriteListResponse> getFavorites(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("즐겨찾기 목록 조회 - 사용자: {}, 페이지: {}, 크기: {}", user.getUserId(), page, size);
        
        FavoriteDto.FavoriteListResponse response = favoriteService.getFavorites(user, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 특정 카테고리 즐겨찾기 목록 조회
     */
    @GetMapping("/category/{contentTypeId}")
    public ResponseEntity<FavoriteDto.FavoriteListResponse> getFavoritesByContentType(
            @AuthenticationPrincipal User user,
            @PathVariable Integer contentTypeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("카테고리별 즐겨찾기 조회 - 사용자: {}, 타입: {}, 페이지: {}", user.getUserId(), contentTypeId, page);
        
        FavoriteDto.FavoriteListResponse response = favoriteService.getFavoritesByContentType(user, contentTypeId, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 즐겨찾기 통계 조회
     */
    @GetMapping("/summary")
    public ResponseEntity<FavoriteDto.FavoriteListResponse.Summary> getFavoritesSummary(
            @AuthenticationPrincipal User user) {
        
        log.info("즐겨찾기 통계 조회 - 사용자: {}", user.getUserId());
        
        FavoriteDto.FavoriteListResponse.Summary summary = favoriteService.getFavoritesSummary(user);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * 특정 장소의 즐겨찾기 여부 확인
     */
    @GetMapping("/check/{placeId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Long placeId) {
        
        boolean isFavorite = favoriteService.isFavorite(user, placeId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorite", isFavorite);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 즐겨찾기 제거 (ID로)
     */
    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<Map<String, String>> removeFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Long favoriteId) {
        
        log.info("즐겨찾기 제거 - 사용자: {}, 즐겨찾기 ID: {}", user.getUserId(), favoriteId);
        
        favoriteService.removeFavorite(user, favoriteId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "즐겨찾기가 제거되었습니다.");
        
        return ResponseEntity.ok(response);
    }
}