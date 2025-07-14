package eureca.capstone.project.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;
}

