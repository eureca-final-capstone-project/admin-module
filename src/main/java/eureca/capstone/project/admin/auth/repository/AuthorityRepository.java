package eureca.capstone.project.admin.auth.repository;

import eureca.capstone.project.admin.auth.entity.Authority;
import eureca.capstone.project.admin.auth.entity.UserAuthority;
import eureca.capstone.project.admin.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<Authority, Long>{
    List<Authority> findByNameIn(List<String> name);
}
