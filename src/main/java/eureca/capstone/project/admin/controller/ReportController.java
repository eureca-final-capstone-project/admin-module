package eureca.capstone.project.admin.controller;

import eureca.capstone.project.admin.dto.request.CreateReportRequestDto;
import eureca.capstone.project.admin.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.dto.response.ReportCountDto;
import eureca.capstone.project.admin.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.dto.response.RestrictionDto;
import eureca.capstone.project.admin.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<Page<ReportHistoryDto>> getReportHistoryList(@RequestParam(required = false) String status, Pageable pageable) {
        return ResponseEntity.ok(reportService.getReportHistoryList(status, pageable));
    }

    @GetMapping("/restrictions")
    public ResponseEntity<Page<RestrictionDto>> getRestrictionList(Pageable pageable) {
        return ResponseEntity.ok(reportService.getRestrictionList(pageable));
    }

    @PostMapping
    public ResponseEntity<Void> createReport(@RequestBody CreateReportRequestDto request) {
        reportService.createReportAndProcessWithAI(
                request.getUserId(),
                request.getTransactionFeedId(),
                request.getReportTypeId(),
                request.getReason()
        );
        return ResponseEntity.accepted().build();
    }

    // TODO: 어드민만 가능하도록 권한 설정해야함
    @PatchMapping("/history/{reportHistoryId}/process")
    public ResponseEntity<Void> processReportByAdmin(
            @PathVariable("reportHistoryId") Long reportHistoryId,
            @RequestBody ProcessReportDto request) {
        reportService.processReportByAdmin(reportHistoryId, request);
        return ResponseEntity.ok().build();
    }
}
