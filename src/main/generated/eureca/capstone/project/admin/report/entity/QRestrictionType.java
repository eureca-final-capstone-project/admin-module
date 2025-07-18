package eureca.capstone.project.admin.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRestrictionType is a Querydsl query type for RestrictionType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRestrictionType extends EntityPathBase<RestrictionType> {

    private static final long serialVersionUID = 48982003L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRestrictionType restrictionType = new QRestrictionType("restrictionType");

    public final eureca.capstone.project.admin.auth.entity.QAuthority authority;

    public final StringPath content = createString("content");

    public final NumberPath<Integer> duration = createNumber("duration", Integer.class);

    public final NumberPath<Long> restrictionTypeId = createNumber("restrictionTypeId", Long.class);

    public QRestrictionType(String variable) {
        this(RestrictionType.class, forVariable(variable), INITS);
    }

    public QRestrictionType(Path<? extends RestrictionType> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRestrictionType(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRestrictionType(PathMetadata metadata, PathInits inits) {
        this(RestrictionType.class, metadata, inits);
    }

    public QRestrictionType(Class<? extends RestrictionType> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.authority = inits.isInitialized("authority") ? new eureca.capstone.project.admin.auth.entity.QAuthority(forProperty("authority")) : null;
    }

}

