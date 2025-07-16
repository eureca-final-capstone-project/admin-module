package eureca.capstone.project.admin.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Status extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long statusId;

    @Column(nullable = false, length = 50)
    private String domain;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String description;
}
