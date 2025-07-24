package eureca.capstone.project.admin.report.repository;

import eureca.capstone.project.admin.report.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportTypeRepository extends JpaRepository<ReportType, Long> {
    ReportType findByType(String name);
}
