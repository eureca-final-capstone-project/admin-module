package eureca.capstone.project.admin.report.repository;

import eureca.capstone.project.admin.auth.entity.Authority;
import eureca.capstone.project.admin.report.entity.RestrictionAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestrictionAuthorityRepository extends JpaRepository<RestrictionAuthority, Long> {
    @Query("select ra.authority from RestrictionAuthority ra where ra.restrictionType.restrictionTypeId = :restrictionTypeId")
    List<Authority> findAuthoritiesByRestrictionTypeId(@Param("restrictionTypeId") Long restrictionTypeId);
}
