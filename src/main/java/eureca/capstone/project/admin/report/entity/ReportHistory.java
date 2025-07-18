package eureca.capstone.project.admin.report.entity;

import eureca.capstone.project.admin.common.entity.BaseEntity;
import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.admin.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_history_id")
    private Long reportHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_type_id")
    private ReportType reportType;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "seller_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User seller;

    @JoinColumn(name = "transaction_feed_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private TransactionFeed transactionFeed;

    @Column(nullable = false)
    private String reason;

    @JoinColumn(name = "status_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Status status;

    @Column(name = "is_moderated")
    private boolean isModerated = false;

    @JoinColumn(name = "restriction_target_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private RestrictionTarget restrictionTarget;


    @Builder
    public ReportHistory(ReportType reportType, User user, User seller, TransactionFeed transactionFeed, String reason, Status status, Boolean isModerated, RestrictionTarget restrictionTarget) {
        this.reportType = reportType;
        this.user = user;
        this.seller = seller;
        this.transactionFeed = transactionFeed;
        this.reason = reason;
        this.status = status;
        this.isModerated = isModerated;
        this.restrictionTarget = restrictionTarget;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void updateRestrictionTarget(RestrictionTarget restrictionTarget) {
        this.restrictionTarget = restrictionTarget;
    }

}

