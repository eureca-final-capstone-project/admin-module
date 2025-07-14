package eureca.capstone.project.admin.domain.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestrictionTargetStatus {
    PENDING("제재 대기중"),
    ACCEPTED("승인"),
    REJECTED("미승인");

    private final String description;

    public static RestrictionTargetStatus from(String description) {
        for (RestrictionTargetStatus status : values()) {
            if (status.getDescription().equals(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + description);
    }
}
