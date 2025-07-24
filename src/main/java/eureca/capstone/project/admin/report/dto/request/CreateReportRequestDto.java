package eureca.capstone.project.admin.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "게시글 신고 요청 DTO")
public class CreateReportRequestDto {
    @Schema(description = "신고 대상 게시글 ID", example = "1")
    private Long transactionFeedId;
    @Schema(description = "신고 유형 ID (1: 욕설 및 비속어 포함, 2: 주제 불일치, 3: 음란 내용 포함, 4: 외부 채널 유도, 5: 비방/저격 포함)", example = "1")
    private Long reportTypeId;
    @Schema(description = "신고 사유 ", example = "욕설이 있어요.")
    private String reason;
}
