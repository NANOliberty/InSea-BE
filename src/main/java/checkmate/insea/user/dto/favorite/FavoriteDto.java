package checkmate.insea.user.dto.favorite;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import checkmate.insea.user.model.UserFavorite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class FavoriteDto {
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddRequest {
        private Long placeId;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RemoveRequest {
        private Long placeId;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FavoriteInfo {
        private Long favoriteId;
        private PlaceInfo place;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        public static FavoriteInfo from(UserFavorite favorite) {
            return FavoriteInfo.builder()
                    .favoriteId(favorite.getId())
                    .place(PlaceInfo.from(favorite.getPlace()))
                    .createdAt(favorite.getCreatedAt())
                    .build();
        }
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaceInfo {
        private Long id;
        private Long contentId;
        private Integer contentTypeId;
        private String contentTypeName;
        private String name;
        private String address;
        private String phone;
        private String mainImageUrl;
        private String thumbImageUrl;
        private String overview;
        private BigDecimal averageRating;
        private Integer totalReviews;
        
        public static PlaceInfo from(checkmate.insea.map.model.Place place) {
            return PlaceInfo.builder()
                    .id(place.getId())
                    .contentId(place.getContentId())
                    .contentTypeId(place.getContentTypeId())
                    .contentTypeName(getContentTypeName(place.getContentTypeId()))
                    .name(place.getName())
                    .address(place.getAddress())
                    .phone(place.getPhone())
                    .mainImageUrl(place.getMainImageUrl())
                    .thumbImageUrl(place.getThumbImageUrl())
                    .overview(place.getOverview())
                    .averageRating(place.getAverageRating())
                    .totalReviews(place.getTotalReviews())
                    .build();
        }
        
        private static String getContentTypeName(Integer contentTypeId) {
            if (contentTypeId == null) return "기타";
            
            return switch (contentTypeId) {
                case 28 -> "레포츠";
                case 32 -> "숙박";
                case 39 -> "음식점";
                default -> "기타";
            };
        }
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FavoriteListResponse {
        private List<FavoriteInfo> favorites;
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private boolean hasNext;
        private boolean hasPrevious;
        
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Summary {
            private long totalFavorites;
            private long sportsCount;    // 레포츠 (28)
            private long accommodationCount; // 숙박 (32)
            private long restaurantCount;    // 음식점 (39)
        }
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ToggleResponse {
        private boolean isFavorite;
        private String message;
        private Long favoriteId; // 추가된 경우에만
    }
}