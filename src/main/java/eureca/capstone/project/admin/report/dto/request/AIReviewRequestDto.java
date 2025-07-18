package eureca.capstone.project.admin.report.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AIReviewRequestDto {
    private String title;
    private String content;
    private String reportContent;
    private String reportType;
}
