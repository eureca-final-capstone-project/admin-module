package eureca.capstone.project.admin.domain.user.entity;

import eureca.capstone.project.admin.domain.common.entry.BaseEntity;
import eureca.capstone.project.admin.domain.common.entry.Status;
import eureca.capstone.project.admin.domain.common.entry.TelecomCompany;
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
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
// TODO 이메일에 유니크 제약조건 거는건 어떠한가
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    private TelecomCompany telecomCompany;

    private String email;
    private String password;
    private String nickname;

    @Column(name = "phone_number")
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private Status status;

    private String provider;

    public void updateUserNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateUserPassword(String password) {
        this.password = password;
    }
}
