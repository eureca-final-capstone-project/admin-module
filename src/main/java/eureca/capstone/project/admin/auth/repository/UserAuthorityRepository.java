package eureca.capstone.project.admin.auth.repository;

import eureca.capstone.project.admin.auth.entity.Authority;
import eureca.capstone.project.admin.auth.entity.UserAuthority;
import eureca.capstone.project.admin.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Long>{
    UserAuthority findByUserAndAuthority(User user, Authority authority);
}
