package eureca.capstone.project.admin.repository;

import eureca.capstone.project.admin.domain.RestrictionTarget;
import eureca.capstone.project.admin.domain.status.RestrictionTargetStatus;
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
            @Param("status") RestrictionTargetStatus status
    );

    @Modifying
    @Query("UPDATE RestrictionTarget rt SET rt.status = :status WHERE rt.restrictionTargetId IN :ids")
    int updateStatusForIds(@Param("ids") List<Long> ids, @Param("status") RestrictionTargetStatus status);
}
