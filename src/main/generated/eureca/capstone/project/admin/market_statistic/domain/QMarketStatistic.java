package eureca.capstone.project.admin.market_statistic.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMarketStatistic is a Querydsl query type for MarketStatistic
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMarketStatistic extends EntityPathBase<MarketStatistic> {

    private static final long serialVersionUID = 1247292585L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMarketStatistic marketStatistic = new QMarketStatistic("marketStatistic");

    public final eureca.capstone.project.admin.common.entity.QBaseEntity _super = new eureca.capstone.project.admin.common.entity.QBaseEntity(this);

    public final NumberPath<Double> averagePrice = createNumber("averagePrice", Double.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> staticsTime = createDateTime("staticsTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> statisticsId = createNumber("statisticsId", Long.class);

    public final eureca.capstone.project.admin.common.entity.QTelecomCompany telecomCompany;

    public final NumberPath<Long> transactionAmount = createNumber("transactionAmount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMarketStatistic(String variable) {
        this(MarketStatistic.class, forVariable(variable), INITS);
    }

    public QMarketStatistic(Path<? extends MarketStatistic> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMarketStatistic(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMarketStatistic(PathMetadata metadata, PathInits inits) {
        this(MarketStatistic.class, metadata, inits);
    }

    public QMarketStatistic(Class<? extends MarketStatistic> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.telecomCompany = inits.isInitialized("telecomCompany") ? new eureca.capstone.project.admin.common.entity.QTelecomCompany(forProperty("telecomCompany")) : null;
    }

}

