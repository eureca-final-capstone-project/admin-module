package eureca.capstone.project.admin.report.service;

import eureca.capstone.project.admin.report.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.report.dto.response.ReportCountDto;
import eureca.capstone.project.admin.report.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.report.dto.response.RestrictExpiredResponseDto;
import eureca.capstone.project.admin.report.dto.response.RestrictionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface ReportService {

    ReportCountDto getReportCounts();
    Page<ReportHistoryDto> getReportHistoryListByStatusCode(String statusCode, Pageable pageable);
    Page<RestrictionDto> getRestrictionListByStatusCode(String statusCode, Pageable pageable);
    void processReportByAdmin(Long reportHistoryId, ProcessReportDto request);
    void createReportAndProcessWithAI(Long userId, Long transactionFeedId, Long reportTypeId, String reason);
    RestrictExpiredResponseDto getRestrictExpiredList();
    void expireRestrictions(List<Long> restrictionTargetIds);
    // 제재 승인: 제재 대상에 올라온 사용자를 승인하면
    // 1. 상태: 제재중,
    // 2. restriction_type: 자동설정된거,
    // 3. expires_at: 만료일
    //
}
