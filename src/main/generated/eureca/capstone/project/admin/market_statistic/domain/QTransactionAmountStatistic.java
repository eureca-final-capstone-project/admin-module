package eureca.capstone.project.admin.market_statistic.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTransactionAmountStatistic is a Querydsl query type for TransactionAmountStatistic
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTransactionAmountStatistic extends EntityPathBase<TransactionAmountStatistic> {

    private static final long serialVersionUID = -809840187L;

    public static final QTransactionAmountStatistic transactionAmountStatistic = new QTransactionAmountStatistic("transactionAmountStatistic");

    public final eureca.capstone.project.admin.common.entity.QBaseEntity _super = new eureca.capstone.project.admin.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> staticsTime = createDateTime("staticsTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> statisticsId = createNumber("statisticsId", Long.class);

    public final NumberPath<Long> transactionAmount = createNumber("transactionAmount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTransactionAmountStatistic(String variable) {
        super(TransactionAmountStatistic.class, forVariable(variable));
    }

    public QTransactionAmountStatistic(Path<? extends TransactionAmountStatistic> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTransactionAmountStatistic(PathMetadata metadata) {
        super(TransactionAmountStatistic.class, metadata);
    }

}

