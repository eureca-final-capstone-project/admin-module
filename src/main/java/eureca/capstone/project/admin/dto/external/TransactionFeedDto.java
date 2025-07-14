package eureca.capstone.project.admin.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFeedDto {
    private String title;
    private String content;
    private Long sellerId;
}
