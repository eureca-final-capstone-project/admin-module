package eureca.capstone.project.admin.report.repository;

import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import eureca.capstone.project.admin.common.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RestrictionTargetRepository extends JpaRepository<RestrictionTarget, Long> {
    @Query("SELECT rt FROM RestrictionTarget rt WHERE rt.expiresAt IS NOT NULL AND rt.expiresAt < :now AND rt.status = :status")
    List<RestrictionTarget> findExpiredRestrictions(
            @Param("now") LocalDateTime now,
            @Param("status") Status status
    );

    @Modifying
    @Query("UPDATE RestrictionTarget rt SET rt.status = :status WHERE rt.restrictionTargetId IN :ids")
    int updateStatusForIds(@Param("ids") List<Long> ids, @Param("status") Status status);

    Page<RestrictionTarget> findByStatus(Status status, Pageable pageable);
}
