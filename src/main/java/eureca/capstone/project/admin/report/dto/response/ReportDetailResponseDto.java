package eureca.capstone.project.admin.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "신고 내역 상세 응답 DTO")
public class ReportDetailResponseDto {
    @Schema(description = "신고 ID", example = "1")
    private Long reportId;
    @Schema(description = "신고 처리 상태 (PENDING, AI_ACCEPTED, AI_REJECTED, ADMIN_ACCEPTED, ADMIN_REJECTED, COMPLETED, REJECTED)", example = "PENDING")
    private String status;
    @Schema(description = "신고자 이메일", example = "reporter@example.com")
    private String reporterEmail;
    @Schema(description = "신고 접수 시간", example = "2023-10-26T10:00:00")
    private LocalDateTime reportDate;
    @Schema(description = "신고 유형", example = "욕설 및 비속어 포함")
    private String reportType;
    @Schema(description = "신고 사유", example = "부적절한 언어 사용")
    private String reportContent;
    @Schema(description = "게시글 통신사", example = "SKT")
    private String telecomCompany;
    @Schema(description = "게시글 데이터 양 (MB)", example = "1000")
    private Long dataAmount;
    @Schema(description = "게시글 제목", example = "판매 게시글 제목")
    private String title;
    @Schema(description = "게시글 내용", example = "판매 게시글 내용입니다.")
    private String content;
    @Schema(description = "판매자 이메일", example = "seller@example.com")
    private String sellerEmail;
    @Schema(description = "게시글 작성 시간", example = "2023-10-25T09:00:00")
    private LocalDateTime feedDate;
    @Schema(description = "게시글 가격", example = "5000")
    private Long price;

}
