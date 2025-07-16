package eureca.capstone.project.admin.user.repository;

import eureca.capstone.project.admin.user.entity.User;
import eureca.capstone.project.admin.user.repository.custom.UserRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
}
