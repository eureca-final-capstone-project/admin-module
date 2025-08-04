package eureca.capstone.project.admin.user.entity;

import eureca.capstone.project.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_data")
public class UserData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userDataId;

    private Long userId;

    @JoinColumn(name = "plan_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Plan plan;

    private Long totalDataMb; // 총 소유 데이터
    private Long sellableDataMb; // 판매 가능한 데이터
    private Long buyerDataMb; // 구매한 데이터
    private Integer resetDataAt; // 데이터 초기화 날짜

    public void addSellableData(long amount) {
        this.sellableDataMb += amount;
    }

}
