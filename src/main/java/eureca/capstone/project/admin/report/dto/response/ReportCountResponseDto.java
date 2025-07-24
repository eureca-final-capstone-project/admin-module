package eureca.capstone.project.admin.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "신고 건수 응답 DTO")
public class ReportCountResponseDto {
    @Schema(description = "오늘 접수된 신고 건수", example = "10")
    private long todayReportCount;
    @Schema(description = "총 신고 건수", example = "100")
    private long totalReportCount;
}
