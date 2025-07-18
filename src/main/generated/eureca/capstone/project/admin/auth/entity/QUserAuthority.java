package eureca.capstone.project.admin.auth.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserAuthority is a Querydsl query type for UserAuthority
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserAuthority extends EntityPathBase<UserAuthority> {

    private static final long serialVersionUID = 1896727889L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserAuthority userAuthority = new QUserAuthority("userAuthority");

    public final eureca.capstone.project.admin.common.entity.QBaseEntity _super = new eureca.capstone.project.admin.common.entity.QBaseEntity(this);

    public final QAuthority authority;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.admin.user.entity.QUser user;

    public final NumberPath<Long> userAuthorityId = createNumber("userAuthorityId", Long.class);

    public QUserAuthority(String variable) {
        this(UserAuthority.class, forVariable(variable), INITS);
    }

    public QUserAuthority(Path<? extends UserAuthority> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserAuthority(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserAuthority(PathMetadata metadata, PathInits inits) {
        this(UserAuthority.class, metadata, inits);
    }

    public QUserAuthority(Class<? extends UserAuthority> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.authority = inits.isInitialized("authority") ? new QAuthority(forProperty("authority")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.admin.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

