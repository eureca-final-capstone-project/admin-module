package eureca.capstone.project.admin.transaction_feed.entity;

import eureca.capstone.project.admin.common.entity.BaseEntity;
import jakarta.persistence.*;

@Table(name = "sales_type")
@Entity
public class SalesType extends BaseEntity {
    @Column(name = "sales_type_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long SalesTypeId;
    private String name;
}
