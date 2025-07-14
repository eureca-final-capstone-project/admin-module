package eureca.capstone.project.admin.dto.response;

import eureca.capstone.project.admin.domain.RestrictionTarget;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RestrictExpiredResponseDto {
    private List<ExpiredRestrictionInfo> expiredRestrictions;

    // 만료된 개별 제재 정보를 담을 내부 클래스
    @Getter
    public static class ExpiredRestrictionInfo {
        private Long restrictionTargetId;
        private Long userId;
        private String restrictionContent;

        public static ExpiredRestrictionInfo from(RestrictionTarget target) {
            ExpiredRestrictionInfo info = new ExpiredRestrictionInfo();
            info.restrictionTargetId = target.getRestrictionTargetId();
            info.userId = target.getUserId();
            info.restrictionContent = target.getRestrictionType().getContent();
            return info;
        }
    }
}
