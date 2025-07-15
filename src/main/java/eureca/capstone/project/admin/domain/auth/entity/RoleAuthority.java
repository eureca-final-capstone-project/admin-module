package eureca.capstone.project.admin.domain.auth.entity;

import eureca.capstone.project.admin.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role_authority")
public class RoleAuthority extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long role_authority_id;

    @JoinColumn(name = "role_id") // 원하는 컬럼명 지정
    @ManyToOne(fetch = FetchType.LAZY)
    private Role role;

    @JoinColumn(name = "authority_id") // 원하는 컬럼명 지정
    @ManyToOne(fetch = FetchType.LAZY)
    private Authority authority;
}
