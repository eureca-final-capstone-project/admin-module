package eureca.capstone.project.admin.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRestrictionAuthority is a Querydsl query type for RestrictionAuthority
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRestrictionAuthority extends EntityPathBase<RestrictionAuthority> {

    private static final long serialVersionUID = -1204372950L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRestrictionAuthority restrictionAuthority = new QRestrictionAuthority("restrictionAuthority");

    public final eureca.capstone.project.admin.auth.entity.QAuthority authority;

    public final NumberPath<Long> restrictionAuthorityId = createNumber("restrictionAuthorityId", Long.class);

    public final QRestrictionType restrictionType;

    public QRestrictionAuthority(String variable) {
        this(RestrictionAuthority.class, forVariable(variable), INITS);
    }

    public QRestrictionAuthority(Path<? extends RestrictionAuthority> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRestrictionAuthority(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRestrictionAuthority(PathMetadata metadata, PathInits inits) {
        this(RestrictionAuthority.class, metadata, inits);
    }

    public QRestrictionAuthority(Class<? extends RestrictionAuthority> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.authority = inits.isInitialized("authority") ? new eureca.capstone.project.admin.auth.entity.QAuthority(forProperty("authority")) : null;
        this.restrictionType = inits.isInitialized("restrictionType") ? new QRestrictionType(forProperty("restrictionType")) : null;
    }

}

