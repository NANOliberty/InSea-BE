package checkmate.insea.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import checkmate.insea.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByEmailAndIsActiveTrue(String email);

        
    // 활성 사용자만 조회
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.nickname = :nickname AND u.isActive = true")
    Optional<User> findActiveUserByNickname(@Param("nickname") String nickname);
    
    // 활성 사용자 존재 여부 확인
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.isActive = true")
    boolean existsActiveUserByEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.nickname = :nickname AND u.isActive = true")
    boolean existsActiveUserByNickname(@Param("nickname") String nickname);
    
}