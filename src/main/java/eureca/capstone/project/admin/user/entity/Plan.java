package eureca.capstone.project.admin.user.entity;


import eureca.capstone.project.admin.common.entity.BaseEntity;
import eureca.capstone.project.admin.common.entity.TelecomCompany;
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
@Table(name = "plan")
public class Plan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @ManyToOne(fetch = FetchType.LAZY)
    private TelecomCompany telecomCompany;

    private String planName;
    private Long monthlyDataMb;
}
