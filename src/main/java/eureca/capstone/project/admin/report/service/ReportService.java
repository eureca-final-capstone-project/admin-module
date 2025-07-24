package eureca.capstone.project.admin.report.service;

import eureca.capstone.project.admin.report.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.report.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface ReportService {

    ReportCountResponseDto getReportCounts();
    Page<ReportHistoryDto> getReportHistoryListByStatusCode(String statusCode, String keyword, Pageable pageable);
    Page<RestrictionDto> getRestrictionListByStatusCode(String statusCode,String keyword, Pageable pageable);
    void processReportByAdmin(Long reportHistoryId, ProcessReportDto request);
    void createReportAndProcessWithAI(Long userId, Long transactionFeedId, Long reportTypeId, String reason);
    void acceptRestrictions(Long restrictionTargetId);
    void rejectRestrictions(Long restrictionTargetId);
    ReportDetailResponseDto getReportDetail(Long reportId);
    List<RestrictionReportResponseDto> getRestrictionReportHistory(Long restrictionId);
    List<ReportTypeResponseDto> getReportTypes();
}
