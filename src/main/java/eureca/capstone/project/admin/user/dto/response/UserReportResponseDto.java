package eureca.capstone.project.admin.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserReportResponseDto {
    private Long reportId;
    private String reportType;
    private String content;
    private LocalDateTime createdAt;
    private String status;
}
