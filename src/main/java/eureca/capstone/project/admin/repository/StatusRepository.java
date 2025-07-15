package eureca.capstone.project.admin.repository;

import eureca.capstone.project.admin.domain.common.entry.Status;
import eureca.capstone.project.admin.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
    Status findByCode(String statusName);
}
