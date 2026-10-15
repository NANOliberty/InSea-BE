package checkmate.insea.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import checkmate.insea.user.model.UserFavorite;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    
    // 특정 사용자의 즐겨찾기 목록 조회 (페이징)
    @Query("SELECT uf FROM UserFavorite uf JOIN FETCH uf.place p WHERE uf.user.userId = :userId ORDER BY uf.createdAt DESC")
    Page<UserFavorite> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    // 특정 사용자의 특정 컨텐츠 타입 즐겨찾기 목록 조회
    @Query("SELECT uf FROM UserFavorite uf JOIN FETCH uf.place p WHERE uf.user.userId = :userId AND p.contentTypeId = :contentTypeId ORDER BY uf.createdAt DESC")
    Page<UserFavorite> findByUserIdAndContentTypeIdOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("contentTypeId") Integer contentTypeId, Pageable pageable);
    
    // 즐겨찾기 여부 확인
    @Query("SELECT uf FROM UserFavorite uf WHERE uf.user.userId = :userId AND uf.place.id = :placeId")
    Optional<UserFavorite> findByUserIdAndPlaceId(@Param("userId") Long userId, @Param("placeId") Long placeId);
    
    // 즐겨찾기 존재 여부 확인
    boolean existsByUserUserIdAndPlaceId(Long userId, Long placeId);
    
    // 특정 사용자의 즐겨찾기 개수
    long countByUserUserId(Long userId);
    
    // 특정 사용자의 특정 컨텐츠 타입 즐겨찾기 개수
    @Query("SELECT COUNT(uf) FROM UserFavorite uf WHERE uf.user.userId = :userId AND uf.place.contentTypeId = :contentTypeId")
    long countByUserIdAndContentTypeId(@Param("userId") Long userId, @Param("contentTypeId") Integer contentTypeId);
    
    // 즐겨찾기 삭제
    @Modifying
    @Query("DELETE FROM UserFavorite uf WHERE uf.user.userId = :userId AND uf.place.id = :placeId")
    void deleteByUserIdAndPlaceId(@Param("userId") Long userId, @Param("placeId") Long placeId);
    
    // 사용자 탈퇴 시 모든 즐겨찾기 삭제
    @Modifying
    @Query("DELETE FROM UserFavorite uf WHERE uf.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}