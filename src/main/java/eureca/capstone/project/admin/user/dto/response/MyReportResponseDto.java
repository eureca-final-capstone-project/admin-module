package eureca.capstone.project.admin.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "내가 신고한 내역 응답 DTO")
public class MyReportResponseDto {

    @Schema(description = "신고 대상 게시글 ID", example = "1")
    private Long transactionFeedId;
    @Schema(description = "신고 대상 게시글 제목", example = "판매 게시글 제목")
    private String title;
    @Schema(description = "판매 데이터 양 (MB)", example = "1000")
    private Long salesDataAmount;
    @Schema(description = "신고 유형", example = "욕설 및 비속어 포함")
    private String reportType;
    @Schema(description = "신고 접수 시간", example = "2023-10-26T10:00:00")
    private LocalDateTime createdAt;
    @Schema(description = "신고 처리 상태 (PENDING, AI_ACCEPTED, AI_REJECTED, ADMIN_ACCEPTED, ADMIN_REJECTED, COMPLETED, REJECTED)", example = "PENDING")
    private String status;
    @Schema(description = "신고 사유", example = "부적절한 언어 사용")
    private String reason;
}
