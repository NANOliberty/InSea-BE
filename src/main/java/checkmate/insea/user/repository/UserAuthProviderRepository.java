package checkmate.insea.user.repository;

import checkmate.insea.user.model.User;
import checkmate.insea.user.model.UserAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Long> {
    Optional<UserAuthProvider> findByProviderAndProviderUserId(String provider, String providerUserId);
    List<UserAuthProvider> findByUser(User user);
    List<UserAuthProvider> findByUserUserId(Long userId);
    void deleteByUserUserId(Long userId);
}
