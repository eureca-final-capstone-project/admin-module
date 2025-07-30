package eureca.capstone.project.admin.transaction_feed.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSalesType is a Querydsl query type for SalesType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSalesType extends EntityPathBase<SalesType> {

    private static final long serialVersionUID = -1844240472L;

    public static final QSalesType salesType = new QSalesType("salesType");

    public final eureca.capstone.project.admin.common.entity.QBaseEntity _super = new eureca.capstone.project.admin.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath name = createString("name");

    public final NumberPath<Long> salesTypeId = createNumber("salesTypeId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSalesType(String variable) {
        super(SalesType.class, forVariable(variable));
    }

    public QSalesType(Path<? extends SalesType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSalesType(PathMetadata metadata) {
        super(SalesType.class, metadata);
    }

}

