package eureca.capstone.project.admin.domain;

import eureca.capstone.project.admin.domain.status.ReportHistoryStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportHistoryId;

    @ManyToOne
    @JoinColumn(name = "report_type_id")
    private ReportType reportType;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long transactionFeedId;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportHistoryStatus status;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isModerated = false;

    @Builder
    public ReportHistory(ReportType reportType, Long userId, Long transactionFeedId, String reason, ReportHistoryStatus status) {
        this.reportType = reportType;
        this.userId = userId;
        this.transactionFeedId = transactionFeedId;
        this.reason = reason;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public void approveByAdmin() {
        this.status = ReportHistoryStatus.ADMIN_ACCEPTED;
    }

    public void rejectByAdmin() {
        this.status = ReportHistoryStatus.ADMIN_REJECTED;
    }
}

