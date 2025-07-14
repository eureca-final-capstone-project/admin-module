package eureca.capstone.project.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restriction_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestrictionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restrictionTypeId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer duration;

}

