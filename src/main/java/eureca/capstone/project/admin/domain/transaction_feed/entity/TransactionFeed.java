package eureca.capstone.project.admin.domain.transaction_feed.entity;

import eureca.capstone.project.admin.domain.common.entity.BaseEntity;
import eureca.capstone.project.admin.domain.common.entity.Status;
import eureca.capstone.project.admin.domain.common.entity.TelecomCompany;
import eureca.capstone.project.admin.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Table(name = "transaction_feed")
@Entity
@Getter
@Builder
public class TransactionFeed extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "transaction_feed_id")
    private Long transactionFeedId;

    @JoinColumn(name = "seller_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name="title")
    private String title;

    @Column(name="content")
    private String content;

    @JoinColumn(name = "telecome_company_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private TelecomCompany telecomCompany;

    @JoinColumn(name = "sales_type_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private SalesType salesType;

    @Column(name = "sales_price")
    private Long salesPrice;

    @Column(name = "sales_data_amount")
    private Long salesDataAmount;

    @Column(name = "default_image_number")
    private Long defaultImageNumber;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @JoinColumn(name = "status_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Status status;

    @Column(name = "is_deleted")
    private boolean isDeleted;
}