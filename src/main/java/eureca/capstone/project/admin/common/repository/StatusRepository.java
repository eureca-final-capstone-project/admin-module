package eureca.capstone.project.admin.common.repository;

import eureca.capstone.project.admin.common.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Long> {
    Status findByCode(String statusName);

    Optional<Status> findByDomainAndCode(String restriction, String statusCode);
}
