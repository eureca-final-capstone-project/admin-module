package eureca.capstone.project.admin.controller;

import eureca.capstone.project.admin.dto.response.ReportCountDto;
import eureca.capstone.project.admin.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.dto.response.RestrictionDto;
import eureca.capstone.project.admin.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/counts")
    public ResponseEntity<ReportCountDto> getReportCounts() {
        return ResponseEntity.ok(reportService.getReportCounts());
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ReportHistoryDto>> getReportHistoryList(Pageable pageable) {
        return ResponseEntity.ok(reportService.getReportHistoryList(pageable));
    }

    @GetMapping("/restrictions")
    public ResponseEntity<Page<RestrictionDto>> getRestrictionList(Pageable pageable) {
        return ResponseEntity.ok(reportService.getRestrictionList(pageable));
    }
}
