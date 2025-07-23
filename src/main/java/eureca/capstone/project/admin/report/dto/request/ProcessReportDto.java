package eureca.capstone.project.admin.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "관리자 신고 처리 요청 DTO")
public class ProcessReportDto {
    @Schema(description = "신고 승인 여부. true: 승인, false: 거절", example = "true")
    private Boolean approved;
}
