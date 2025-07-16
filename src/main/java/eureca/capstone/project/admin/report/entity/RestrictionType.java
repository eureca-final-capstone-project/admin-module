package eureca.capstone.project.admin.report.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restriction_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestrictionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="restriction_type_id")
    private Long restrictionTypeId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer duration;

    @Builder
    public RestrictionType(Long restrictionTypeId, String content, Integer duration) {
        this.restrictionTypeId = restrictionTypeId;
        this.content = content;
        this.duration = duration;
    }
}

