package eureca.capstone.project.admin.report.repository;

import eureca.capstone.project.admin.report.entity.ReportHistory;
import eureca.capstone.project.admin.report.repository.custom.ReportHistoryRepositoryCustom;
import eureca.capstone.project.admin.report.entity.ReportType;
import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import eureca.capstone.project.admin.report.repository.custom.ReportHistoryRepositoryCustom;
import eureca.capstone.project.admin.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long>, ReportHistoryRepositoryCustom {

    @Query("SELECT COUNT(rh) FROM ReportHistory rh WHERE rh.createdAt >= :startOfDay")
    Long countByCreatedAtAfter(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(rh) FROM ReportHistory rh " +
            "WHERE rh.seller=:seller AND rh.reportType=:type AND rh.status IN :status AND rh.restrictionTarget IS NULL")
    Long countReportToRestrict(@Param("seller") User seller, @Param("type") ReportType reportType, @Param("status") List<Status> statuses);

    @Query("SELECT rh FROM ReportHistory rh " +
            "WHERE rh.seller=:seller AND rh.reportType=:type AND rh.status IN :status AND rh.restrictionTarget IS NULL")
    List<ReportHistory> findReportsToRestrict(@Param("seller") User seller, @Param("type") ReportType reportType, @Param("status") List<Status> statuses);

    Page<ReportHistory> findByStatus(Status status, Pageable pageable);

    boolean existsByUserAndSeller(User user, User seller);


}
