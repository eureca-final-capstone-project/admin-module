package eureca.capstone.project.admin.transaction_feed.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTransactionFeed is a Querydsl query type for TransactionFeed
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTransactionFeed extends EntityPathBase<TransactionFeed> {

    private static final long serialVersionUID = 1339706718L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTransactionFeed transactionFeed = new QTransactionFeed("transactionFeed");

    public final eureca.capstone.project.admin.common.entity.QBaseEntity _super = new eureca.capstone.project.admin.common.entity.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> defaultImageNumber = createNumber("defaultImageNumber", Long.class);

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final NumberPath<Long> salesDataAmount = createNumber("salesDataAmount", Long.class);

    public final NumberPath<Long> salesPrice = createNumber("salesPrice", Long.class);

    public final QSalesType salesType;

    public final eureca.capstone.project.admin.common.entity.QStatus status;

    public final eureca.capstone.project.admin.common.entity.QTelecomCompany telecomCompany;

    public final StringPath title = createString("title");

    public final NumberPath<Long> transactionFeedId = createNumber("transactionFeedId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.admin.user.entity.QUser user;

    public QTransactionFeed(String variable) {
        this(TransactionFeed.class, forVariable(variable), INITS);
    }

    public QTransactionFeed(Path<? extends TransactionFeed> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTransactionFeed(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTransactionFeed(PathMetadata metadata, PathInits inits) {
        this(TransactionFeed.class, metadata, inits);
    }

    public QTransactionFeed(Class<? extends TransactionFeed> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.salesType = inits.isInitialized("salesType") ? new QSalesType(forProperty("salesType")) : null;
        this.status = inits.isInitialized("status") ? new eureca.capstone.project.admin.common.entity.QStatus(forProperty("status")) : null;
        this.telecomCompany = inits.isInitialized("telecomCompany") ? new eureca.capstone.project.admin.common.entity.QTelecomCompany(forProperty("telecomCompany")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.admin.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

