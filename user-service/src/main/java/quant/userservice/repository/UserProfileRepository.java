package quant.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quant.userservice.entity.UserProfile;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByEmail(String email);
}
