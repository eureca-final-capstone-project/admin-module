package eureca.capstone.project.admin.repository;

import eureca.capstone.project.admin.domain.ReportHistory;
import eureca.capstone.project.admin.domain.ReportType;
import eureca.capstone.project.admin.domain.common.entry.Status;
import eureca.capstone.project.admin.domain.user.entity.User;
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

    Long countByUserAndReportTypeAndStatusIn(User user, ReportType reportType, List<Status> statuses);

    Page<ReportHistory> findByStatus(Status status, Pageable pageable);

    boolean existsByUserAndSeller(User user, User seller);
}
