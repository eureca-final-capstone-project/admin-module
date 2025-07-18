package eureca.capstone.project.admin.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReportHistory is a Querydsl query type for ReportHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReportHistory extends EntityPathBase<ReportHistory> {

    private static final long serialVersionUID = 1298598605L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReportHistory reportHistory = new QReportHistory("reportHistory");

    public final eureca.capstone.project.admin.common.entity.QBaseEntity _super = new eureca.capstone.project.admin.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath isModerated = createBoolean("isModerated");

    public final StringPath reason = createString("reason");

    public final NumberPath<Long> reportHistoryId = createNumber("reportHistoryId", Long.class);

    public final QReportType reportType;

    public final QRestrictionTarget restrictionTarget;

    public final eureca.capstone.project.admin.user.entity.QUser seller;

    public final eureca.capstone.project.admin.common.entity.QStatus status;

    public final eureca.capstone.project.admin.transaction_feed.entity.QTransactionFeed transactionFeed;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.admin.user.entity.QUser user;

    public QReportHistory(String variable) {
        this(ReportHistory.class, forVariable(variable), INITS);
    }

    public QReportHistory(Path<? extends ReportHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReportHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReportHistory(PathMetadata metadata, PathInits inits) {
        this(ReportHistory.class, metadata, inits);
    }

    public QReportHistory(Class<? extends ReportHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reportType = inits.isInitialized("reportType") ? new QReportType(forProperty("reportType")) : null;
        this.restrictionTarget = inits.isInitialized("restrictionTarget") ? new QRestrictionTarget(forProperty("restrictionTarget"), inits.get("restrictionTarget")) : null;
        this.seller = inits.isInitialized("seller") ? new eureca.capstone.project.admin.user.entity.QUser(forProperty("seller"), inits.get("seller")) : null;
        this.status = inits.isInitialized("status") ? new eureca.capstone.project.admin.common.entity.QStatus(forProperty("status")) : null;
        this.transactionFeed = inits.isInitialized("transactionFeed") ? new eureca.capstone.project.admin.transaction_feed.entity.QTransactionFeed(forProperty("transactionFeed"), inits.get("transactionFeed")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.admin.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

