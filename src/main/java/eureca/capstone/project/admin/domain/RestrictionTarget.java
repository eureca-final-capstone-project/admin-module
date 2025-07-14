package eureca.capstone.project.admin.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "restriction_target")
public class RestrictionTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restrictionTargetId;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "report_type_id")
    private ReportType reportType;

    @ManyToOne
    @JoinColumn(name = "restriction_type_id")
    private RestrictionType restrictionType;

    @Column(nullable = false)
    private String status;

    private LocalDateTime expiresAt;
}

