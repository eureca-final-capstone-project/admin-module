package eureca.capstone.project.admin.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProcessReportDto {
    @Schema(description = "신고 승인여부. approved=true: 승인, approved=false: 거절")
    private Boolean approved;
}
