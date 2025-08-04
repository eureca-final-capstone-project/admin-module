package eureca.capstone.project.admin.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserData is a Querydsl query type for UserData
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserData extends EntityPathBase<UserData> {

    private static final long serialVersionUID = -1968928513L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserData userData = new QUserData("userData");

    public final eureca.capstone.project.admin.common.entity.QBaseEntity _super = new eureca.capstone.project.admin.common.entity.QBaseEntity(this);

    public final NumberPath<Long> buyerDataMb = createNumber("buyerDataMb", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QPlan plan;

    public final NumberPath<Integer> resetDataAt = createNumber("resetDataAt", Integer.class);

    public final NumberPath<Long> sellableDataMb = createNumber("sellableDataMb", Long.class);

    public final NumberPath<Long> totalDataMb = createNumber("totalDataMb", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userDataId = createNumber("userDataId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUserData(String variable) {
        this(UserData.class, forVariable(variable), INITS);
    }

    public QUserData(Path<? extends UserData> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserData(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserData(PathMetadata metadata, PathInits inits) {
        this(UserData.class, metadata, inits);
    }

    public QUserData(Class<? extends UserData> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.plan = inits.isInitialized("plan") ? new QPlan(forProperty("plan"), inits.get("plan")) : null;
    }

}

