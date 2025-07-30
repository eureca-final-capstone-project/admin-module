package eureca.capstone.project.admin.market_statistic.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTransactionAmountStatistic is a Querydsl query type for TransactionAmountStatistic
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTransactionAmountStatistic extends EntityPathBase<TransactionAmountStatistic> {

    private static final long serialVersionUID = -809840187L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTransactionAmountStatistic transactionAmountStatistic = new QTransactionAmountStatistic("transactionAmountStatistic");

    public final eureca.capstone.project.admin.common.entity.QBaseEntity _super = new eureca.capstone.project.admin.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final eureca.capstone.project.admin.transaction_feed.entity.QSalesType salesType;

    public final DateTimePath<java.time.LocalDateTime> staticsTime = createDateTime("staticsTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> statisticsId = createNumber("statisticsId", Long.class);

    public final StringPath statisticType = createString("statisticType");

    public final NumberPath<Long> transactionAmount = createNumber("transactionAmount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTransactionAmountStatistic(String variable) {
        this(TransactionAmountStatistic.class, forVariable(variable), INITS);
    }

    public QTransactionAmountStatistic(Path<? extends TransactionAmountStatistic> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTransactionAmountStatistic(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTransactionAmountStatistic(PathMetadata metadata, PathInits inits) {
        this(TransactionAmountStatistic.class, metadata, inits);
    }

    public QTransactionAmountStatistic(Class<? extends TransactionAmountStatistic> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.salesType = inits.isInitialized("salesType") ? new eureca.capstone.project.admin.transaction_feed.entity.QSalesType(forProperty("salesType")) : null;
    }

}

