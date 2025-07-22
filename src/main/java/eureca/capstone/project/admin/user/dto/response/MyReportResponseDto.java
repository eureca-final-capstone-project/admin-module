package eureca.capstone.project.admin.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyReportResponseDto {

    private Long transactionFeedId;
    private String title;
    private Long salesDataAmount;
    private String reportType;
    private LocalDateTime createdAt;
    private String status;
    private String reason;
}
