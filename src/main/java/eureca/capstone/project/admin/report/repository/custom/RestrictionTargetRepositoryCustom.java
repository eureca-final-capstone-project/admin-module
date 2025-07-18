package eureca.capstone.project.admin.report.repository.custom;

import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestrictionTargetRepositoryCustom {
    Page<RestrictionTarget> findByCriteria(String statusCode, String keyword, Pageable pageable);
}
