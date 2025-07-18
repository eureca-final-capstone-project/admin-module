package eureca.capstone.project.admin.report.repository.custom;

import eureca.capstone.project.admin.report.dto.response.ReportDetailResponseDto;
import eureca.capstone.project.admin.report.dto.response.RestrictionReportResponseDto;
import eureca.capstone.project.admin.report.entity.ReportHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReportHistoryRepositoryCustom {
    Page<ReportHistory> findByCriteria(String statusCode, String keyword, Pageable pageable);
    ReportDetailResponseDto getReportDetail(Long id);
    List<RestrictionReportResponseDto> getRestrictionReportList(Long restrictionId);
}
