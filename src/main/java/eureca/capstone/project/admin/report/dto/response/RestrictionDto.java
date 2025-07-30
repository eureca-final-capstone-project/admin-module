package eureca.capstone.project.admin.report.dto.response;

import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "제재 내역 응답 DTO")
public class RestrictionDto {
    @Schema(description = "제재 대상 ID", example = "1")
    private Long restrictionTargetId;
    @Schema(description = "제재 대상 사용자 ID", example = "100")
    private Long userId;
    @Schema(description = "제재 대상 사용자 이메일", example = "user@example.com")
    private String userEmail;
    @Schema(description = "신고 유형", example = "욕설 및 비속어 포함")
    private String reportType;
    @Schema(description = "제재 내용", example = "게시글 작성 제한")
    private String restrictionContent;
    @Schema(description = "제재 기간 (일)", example = "7")
    private Integer restrictionDuration;
    @Schema(description = "제재 상태 (PENDING, COMPLETED, REJECTED)", example = "PENDING")
    private String status;
    @Schema(description = "제재 만료 시간", example = "2023-11-02T10:00:00")
    private LocalDateTime expiresAt;

    public static RestrictionDto from(RestrictionTarget restrictionTarget) {
        return RestrictionDto.builder()
                .restrictionTargetId(restrictionTarget.getRestrictionTargetId())
                .userId(restrictionTarget.getUser().getUserId())
                .userEmail(restrictionTarget.getUser().getEmail())
                .reportType(restrictionTarget.getReportType().getType())
                .restrictionContent(restrictionTarget.getRestrictionType().getContent())
                .restrictionDuration(restrictionTarget.getRestrictionType().getDuration())
                .status(restrictionTarget.getStatus().getCode())
                .expiresAt(restrictionTarget.getExpiresAt())
                .build();
    }
}
