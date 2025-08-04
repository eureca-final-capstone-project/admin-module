package eureca.capstone.project.admin.user.repository;

import eureca.capstone.project.admin.user.entity.UserData;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserDataRepository extends JpaRepository<UserData, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserData u where u.userId = :userId")
    Optional<UserData> findByUserId(Long userId);
}
