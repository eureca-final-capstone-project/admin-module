package eureca.capstone.project.admin.domain;

import eureca.capstone.project.admin.domain.status.RestrictionTargetStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "restriction_target")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestrictionTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restrictionTargetId;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_type_id")
    private ReportType reportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restriction_type_id")
    private RestrictionType restrictionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestrictionTargetStatus status;

    private LocalDateTime expiresAt;

    @Builder
    public RestrictionTarget(Long userId, ReportType reportType, RestrictionType restrictionType, RestrictionTargetStatus status, LocalDateTime expiresAt) {
        this.userId = userId;
        this.reportType = reportType;
        this.restrictionType = restrictionType;
        this.status = status;
        this.expiresAt = expiresAt;
    }
}

