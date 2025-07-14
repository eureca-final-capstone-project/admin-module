package eureca.capstone.project.admin.domain.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportHistoryStatus {
    PENDING("검수 대기중"),
    AI_ACCEPTED("AI 승인"),
    AI_REJECTED("AI 거절"),
    ADMIN_ACCEPTED("관리자 승인"),
    ADMIN_REJECTED("관리자 거절"),
    RESTRICTION_SUCCESS("제재 완료"),
    RESTRICTION_FAILED("제재 미승인");

    private final String description;

    public static ReportHistoryStatus from(String description) {
        for (ReportHistoryStatus status : values()) {
            if (status.getDescription().equals(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + description);
    }
}

