package eureca.capstone.project.admin.repository;

import eureca.capstone.project.admin.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
