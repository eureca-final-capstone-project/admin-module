package eureca.capstone.project.admin.service;

import eureca.capstone.project.admin.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.dto.response.ReportCountDto;
import eureca.capstone.project.admin.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.dto.response.RestrictExpiredResponseDto;
import eureca.capstone.project.admin.dto.response.RestrictionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface ReportService {

    ReportCountDto getReportCounts();
    Page<ReportHistoryDto> getReportHistoryList(String status, Pageable pageable);
    Page<RestrictionDto> getRestrictionList(Pageable pageable);
    void processReportByAdmin(Long reportHistoryId, ProcessReportDto request);
    void createReportAndProcessWithAI(Long userId, Long transactionFeedId, Long reportTypeId, String reason);
    RestrictExpiredResponseDto getRestrictExpiredList();
    void expireRestrictions(List<Long> restrictionTargetIds);
}
