package eureca.capstone.project.admin.report.repository;

import eureca.capstone.project.admin.report.entity.RestrictionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestrictionTypeRepository extends JpaRepository<RestrictionType, Long> {
    Optional<RestrictionType> findByContent(String content);
}
