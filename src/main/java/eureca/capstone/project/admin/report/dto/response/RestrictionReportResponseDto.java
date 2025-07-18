package eureca.capstone.project.admin.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestrictionReportResponseDto {
    private Long reportId;
    private String reportType;
    private String content;
    private LocalDateTime reportedAt; // 신고일시
    private String status;
}
