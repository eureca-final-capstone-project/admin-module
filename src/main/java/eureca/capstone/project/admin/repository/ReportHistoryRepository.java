package eureca.capstone.project.admin.repository;

import eureca.capstone.project.admin.domain.ReportHistory;
import eureca.capstone.project.admin.domain.ReportType;
import eureca.capstone.project.admin.domain.status.ReportHistoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long> {

    @Query("SELECT COUNT(rh) FROM ReportHistory rh WHERE rh.createdAt >= :startOfDay")
    Long countByCreatedAtAfter(@Param("startOfDay") LocalDateTime startOfDay);

    long countByUserIdAndReportTypeAndStatusIn(Long userId, ReportType reportType, List<ReportHistoryStatus> statuses);

    Page<ReportHistory> findByStatus(ReportHistoryStatus status, Pageable pageable);
}
