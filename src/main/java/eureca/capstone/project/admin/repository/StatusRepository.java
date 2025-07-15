package eureca.capstone.project.admin.repository;

import eureca.capstone.project.admin.domain.common.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
    Status findByCode(String statusName);
}
