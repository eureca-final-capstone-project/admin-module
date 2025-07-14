package eureca.capstone.project.admin.service;

import eureca.capstone.project.admin.dto.response.ReportCountDto;
import eureca.capstone.project.admin.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.dto.response.RestrictionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ReportService {

    ReportCountDto getReportCounts();
    Page<ReportHistoryDto> getReportHistoryList(Pageable pageable);
    Page<RestrictionDto> getRestrictionList(Pageable pageable);
}
