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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_type_id")
    private ReportType reportType;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long sellerId;

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
    public ReportHistory(ReportType reportType, Long userId, Long sellerId, Long transactionFeedId, String reason, ReportHistoryStatus status, Boolean isModerated) {
        this.reportType = reportType;
        this.userId = userId;
        this.sellerId = sellerId;
        this.transactionFeedId = transactionFeedId;
        this.reason = reason;
        this.status = status;
        this.isModerated = isModerated;
        this.createdAt = LocalDateTime.now();
    }

    public void approveByAdmin() {
        this.status = ReportHistoryStatus.ADMIN_ACCEPTED;
    }

    public void rejectByAdmin() {
        this.status = ReportHistoryStatus.ADMIN_REJECTED;
    }
}

