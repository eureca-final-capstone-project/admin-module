package eureca.capstone.project.admin.user.repository;

import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.user.entity.User;
import eureca.capstone.project.admin.user.repository.custom.UserRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    long countByStatus(Status status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
