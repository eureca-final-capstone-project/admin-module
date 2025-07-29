package eureca.capstone.project.admin.report.entity;

import eureca.capstone.project.admin.auth.entity.Authority;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restriction_authority")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestrictionAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restriction_authority_id")
    private Long restrictionAuthorityId;

    @JoinColumn(name = "restriction_type_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private RestrictionType restrictionType;

    @JoinColumn(name = "authority_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Authority authority;
}
