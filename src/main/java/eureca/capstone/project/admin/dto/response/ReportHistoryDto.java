package eureca.capstone.project.admin.dto.response;

import eureca.capstone.project.admin.domain.ReportHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportHistoryDto {
    private Long reportHistoryId;
    private String reportType;
    private Long userId;
    private Long transactionFeedId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    public static ReportHistoryDto from(ReportHistory reportHistory) {
        return ReportHistoryDto.builder()
                .reportHistoryId(reportHistory.getReportHistoryId())
                .reportType(reportHistory.getReportType().getType())
                .userId(reportHistory.getUserId())
                .transactionFeedId(reportHistory.getTransactionFeedId())
                .reason(reportHistory.getReason())
                .status(reportHistory.getStatus().getDescription())
                .createdAt(reportHistory.getCreatedAt())
                .build();
    }
}
