package eureca.capstone.project.admin.domain;

import eureca.capstone.project.admin.domain.common.entity.BaseEntity;
import eureca.capstone.project.admin.domain.common.entity.Status;
import eureca.capstone.project.admin.domain.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.admin.domain.user.entity.User;
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


    @Builder
    public ReportHistory(ReportType reportType, User user, User seller, TransactionFeed transactionFeed, String reason, Status status, Boolean isModerated) {
        this.reportType = reportType;
        this.user = user;
        this.seller = seller;
        this.transactionFeed = transactionFeed;
        this.reason = reason;
        this.status = status;
        this.isModerated = isModerated;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

}

