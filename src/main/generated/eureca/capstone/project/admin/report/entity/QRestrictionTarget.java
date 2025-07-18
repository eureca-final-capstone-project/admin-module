package eureca.capstone.project.admin.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRestrictionTarget is a Querydsl query type for RestrictionTarget
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRestrictionTarget extends EntityPathBase<RestrictionTarget> {

    private static final long serialVersionUID = -195035126L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRestrictionTarget restrictionTarget = new QRestrictionTarget("restrictionTarget");

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final QReportType reportType;

    public final NumberPath<Long> restrictionTargetId = createNumber("restrictionTargetId", Long.class);

    public final QRestrictionType restrictionType;

    public final eureca.capstone.project.admin.common.entity.QStatus status;

    public final eureca.capstone.project.admin.user.entity.QUser user;

    public QRestrictionTarget(String variable) {
        this(RestrictionTarget.class, forVariable(variable), INITS);
    }

    public QRestrictionTarget(Path<? extends RestrictionTarget> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRestrictionTarget(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRestrictionTarget(PathMetadata metadata, PathInits inits) {
        this(RestrictionTarget.class, metadata, inits);
    }

    public QRestrictionTarget(Class<? extends RestrictionTarget> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reportType = inits.isInitialized("reportType") ? new QReportType(forProperty("reportType")) : null;
        this.restrictionType = inits.isInitialized("restrictionType") ? new QRestrictionType(forProperty("restrictionType"), inits.get("restrictionType")) : null;
        this.status = inits.isInitialized("status") ? new eureca.capstone.project.admin.common.entity.QStatus(forProperty("status")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.admin.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

