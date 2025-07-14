package eureca.capstone.project.admin.repository;

import eureca.capstone.project.admin.domain.ReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long> {

    @Query("SELECT COUNT(rh) FROM ReportHistory rh WHERE rh.createdAt >= :startOfDay")
    Long countByCreatedAtAfter(@Param("startOfDay") LocalDateTime startOfDay);
}
