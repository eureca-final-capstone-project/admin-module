package eureca.capstone.project.admin.transaction_feed.entity;

import eureca.capstone.project.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "sales_type")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesType extends BaseEntity {
    @Column(name = "sales_type_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salesTypeId;
    private String name;
}
