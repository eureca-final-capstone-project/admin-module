package eureca.capstone.project.admin.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "제재 ID로 조회한 신고 내역 응답 DTO")
public class RestrictionReportResponseDto {
    @Schema(description = "신고 ID", example = "1")
    private Long reportId;
    @Schema(description = "신고 유형", example = "욕설 및 비속어 포함")
    private String reportType;
    @Schema(description = "신고 내용", example = "부적절한 언어 사용")
    private String content;
    @Schema(description = "신고일시", example = "2023-10-26T10:00:00")
    private LocalDateTime reportedAt;
    @Schema(description = "신고 처리 상태 (PENDING, AI_ACCEPTED, AI_REJECTED, ADMIN_ACCEPTED, ADMIN_REJECTED, COMPLETED, REJECTED)", example = "COMPLETED")
    private String status;
}
