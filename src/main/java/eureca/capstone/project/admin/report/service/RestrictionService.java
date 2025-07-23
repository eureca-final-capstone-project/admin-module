package eureca.capstone.project.admin.report.service;

import eureca.capstone.project.admin.report.dto.response.RestrictionDto;
import eureca.capstone.project.admin.report.dto.response.RestrictionReportResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RestrictionService {
    Page<RestrictionDto> getRestrictionListByStatusCode(String statusCode, String keyword, Pageable pageable);
    void acceptRestrictions(Long restrictionTargetId);
    void rejectRestrictions(Long restrictionTargetId);
    List<RestrictionReportResponseDto> getRestrictionReportHistory(Long restrictionId);
}
