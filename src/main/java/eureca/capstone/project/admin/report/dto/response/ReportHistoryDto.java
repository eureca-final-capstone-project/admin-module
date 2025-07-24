package eureca.capstone.project.admin.report.dto.response;

import eureca.capstone.project.admin.report.entity.ReportHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "신고 내역 응답 DTO")
public class ReportHistoryDto {
    @Schema(description = "신고 내역 ID", example = "1")
    private Long reportHistoryId;
    @Schema(description = "신고 유형", example = "욕설 및 비속어 포함")
    private String reportType;
    @Schema(description = "신고 사유", example = "부적절한 언어 사용")
    private String reason;
    @Schema(description = "신고 처리 상태 (PENDING, AI_ACCEPTED, AI_REJECTED, ADMIN_ACCEPTED, ADMIN_REJECTED, COMPLETED, REJECTED)", example = "PENDING")
    private String status;
    @Schema(description = "신고 접수 시간", example = "2023-10-26T10:00:00")
    private LocalDateTime reportedAt;
    @Schema(description = "신고자 ID", example = "100")
    private Long reporterId;
    @Schema(description = "신고자 이메일", example = "reporter@example.com")
    private String reporterEmail;
    @Schema(description = "신고 대상 게시글 ID", example = "200")
    private Long transactionFeedId;
    @Schema(description = "신고 대상 게시글 제목", example = "판매 게시글 제목")
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
