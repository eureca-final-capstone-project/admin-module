package eureca.capstone.project.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateRestrictionStatusRequestDto {
    private List<Long> restrictionTargetIds;
}
