package eureca.capstone.project.admin.report.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateReportRequestDto {
    private Long transactionFeedId; // 신고 대상 게시글 ID
    private Long reportTypeId;      // 신고 유형 ID
    private String reason;          // 신고 사유
}
