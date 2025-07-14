package eureca.capstone.project.admin.service.impl;

import eureca.capstone.project.admin.dto.response.ReportCountDto;
import eureca.capstone.project.admin.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.dto.response.RestrictionDto;
import eureca.capstone.project.admin.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.repository.RestrictionTargetRepository;
import eureca.capstone.project.admin.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReportHistoryRepository reportHistoryRepository;
    private final RestrictionTargetRepository restrictionTargetRepository;

    @Override
    public ReportCountDto getReportCounts() {
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);

        // 오늘 신고 건수 및 전체 신고 건수 조회
        long todayCount = reportHistoryRepository.countByCreatedAtAfter(startOfToday);
        long totalCount = reportHistoryRepository.count();

        return ReportCountDto.builder()
                .todayReportCount(todayCount)
                .totalReportCount(totalCount)
                .build();
    }

    @Override
    public Page<ReportHistoryDto> getReportHistoryList(Pageable pageable) {
        return reportHistoryRepository.findAll(pageable)
                .map(ReportHistoryDto::from);
    }

    @Override
    public Page<RestrictionDto> getRestrictionList(Pageable pageable) {
        return restrictionTargetRepository.findAll(pageable)
                .map(RestrictionDto::from);
    }
}
