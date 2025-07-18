package eureca.capstone.project.admin.report.dto.response;

import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDetailResponseDto {
    private Long reportId;
    private String status;
    private String reporterEmail; // 신고자 이메일
    private LocalDateTime reportDate;
    private String reportType;
    private String reportContent;
    private String telecomCompany;
    private Long dataAmount;
    private String title;
    private String content;
    private String sellerEmail;
    private LocalDateTime feedDate;
    private Long price;

}
