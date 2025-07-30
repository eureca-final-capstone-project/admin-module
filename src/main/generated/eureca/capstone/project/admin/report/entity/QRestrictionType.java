package eureca.capstone.project.admin.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRestrictionType is a Querydsl query type for RestrictionType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRestrictionType extends EntityPathBase<RestrictionType> {

    private static final long serialVersionUID = 48982003L;

    public static final QRestrictionType restrictionType = new QRestrictionType("restrictionType");

    public final StringPath content = createString("content");

    public final NumberPath<Integer> duration = createNumber("duration", Integer.class);

    public final NumberPath<Long> restrictionTypeId = createNumber("restrictionTypeId", Long.class);

    public QRestrictionType(String variable) {
        super(RestrictionType.class, forVariable(variable));
    }

    public QRestrictionType(Path<? extends RestrictionType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRestrictionType(PathMetadata metadata) {
        super(RestrictionType.class, metadata);
    }

}

