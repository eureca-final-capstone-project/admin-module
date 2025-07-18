package eureca.capstone.project.admin.report.dto.response;

import eureca.capstone.project.admin.report.entity.ReportHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportHistoryDto {
    // 신고 정보
    private Long reportHistoryId;
    private String reportType;
    private String reason;
    private String status;
    private LocalDateTime reportedAt;
    private Long reporterId;
    private String reporterEmail;
    private Long transactionFeedId;
    private String transactionFeedTitle;

    public static ReportHistoryDto from(ReportHistory reportHistory) {
        return ReportHistoryDto.builder()
                .reportHistoryId(reportHistory.getReportHistoryId())
                .reportType(reportHistory.getReportType().getType())
                .reason(reportHistory.getReason())
                .status(reportHistory.getStatus().getDescription())
                .reportedAt(reportHistory.getCreatedAt())
                .reporterId(reportHistory.getUser().getUserId())
                .reporterEmail(reportHistory.getUser().getEmail())
                .transactionFeedId(reportHistory.getTransactionFeed().getTransactionFeedId())
                .transactionFeedTitle(reportHistory.getTransactionFeed().getTitle())
                .build();
    }
}
