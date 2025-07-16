package eureca.capstone.project.admin.auth.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoleAuthority is a Querydsl query type for RoleAuthority
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoleAuthority extends EntityPathBase<RoleAuthority> {

    private static final long serialVersionUID = 826556678L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoleAuthority roleAuthority = new QRoleAuthority("roleAuthority");

    public final eureca.capstone.project.admin.common.entity.QBaseEntity _super = new eureca.capstone.project.admin.common.entity.QBaseEntity(this);

    public final QAuthority authority;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QRole role;

    public final NumberPath<Long> role_authority_id = createNumber("role_authority_id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRoleAuthority(String variable) {
        this(RoleAuthority.class, forVariable(variable), INITS);
    }

    public QRoleAuthority(Path<? extends RoleAuthority> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoleAuthority(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoleAuthority(PathMetadata metadata, PathInits inits) {
        this(RoleAuthority.class, metadata, inits);
    }

    public QRoleAuthority(Class<? extends RoleAuthority> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.authority = inits.isInitialized("authority") ? new QAuthority(forProperty("authority")) : null;
        this.role = inits.isInitialized("role") ? new QRole(forProperty("role")) : null;
    }

}

