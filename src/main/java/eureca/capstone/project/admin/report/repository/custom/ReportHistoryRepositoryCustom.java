package eureca.capstone.project.admin.report.repository.custom;

import eureca.capstone.project.admin.report.dto.response.ReportDetailResponseDto;
import eureca.capstone.project.admin.user.dto.UserInformationDto;
import eureca.capstone.project.admin.user.dto.response.UserReportResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReportHistoryRepositoryCustom {
    ReportDetailResponseDto getReportDetail(Long id);
}
