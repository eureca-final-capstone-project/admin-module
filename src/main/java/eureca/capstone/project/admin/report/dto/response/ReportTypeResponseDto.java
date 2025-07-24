package eureca.capstone.project.admin.report.dto.response;

import eureca.capstone.project.admin.report.entity.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "신고 유형 응답 DTO")
public class ReportTypeResponseDto {

    @Schema(description = "신고 유형 ID", example = "1")
    private Long reportTypeId;

    @Schema(description = "신고 유형 이름", example = "욕설 및 비속어 포함")
    private String typeName;

    public static ReportTypeResponseDto from(ReportType reportType) {
        return ReportTypeResponseDto.builder()
                .reportTypeId(reportType.getReportTypeId())
                .typeName(reportType.getType())
                .build();
    }
}
