package eureca.capstone.project.admin.dto.response;

import eureca.capstone.project.admin.domain.RestrictionTarget;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RestrictionDto {
    private Long restrictionTargetId;
    private Long userId;
    private String reportType;
    private String restrictionContent;
    private Integer restrictionDuration;
    private String status;
    private LocalDateTime expiresAt;

    public static RestrictionDto from(RestrictionTarget restrictionTarget) {
        return RestrictionDto.builder()
                .restrictionTargetId(restrictionTarget.getRestrictionTargetId())
                .userId(restrictionTarget.getUserId())
                .reportType(restrictionTarget.getReportType().getType())
                .restrictionContent(restrictionTarget.getRestrictionType().getContent())
                .restrictionDuration(restrictionTarget.getRestrictionType().getDuration())
                .status(restrictionTarget.getStatus())
                .expiresAt(restrictionTarget.getExpiresAt())
                .build();
    }
}
