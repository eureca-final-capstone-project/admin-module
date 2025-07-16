package eureca.capstone.project.admin.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTelecomCompany is a Querydsl query type for TelecomCompany
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTelecomCompany extends EntityPathBase<TelecomCompany> {

    private static final long serialVersionUID = -2116697104L;

    public static final QTelecomCompany telecomCompany = new QTelecomCompany("telecomCompany");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath name = createString("name");

    public final NumberPath<Long> telecomCompanyId = createNumber("telecomCompanyId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTelecomCompany(String variable) {
        super(TelecomCompany.class, forVariable(variable));
    }

    public QTelecomCompany(Path<? extends TelecomCompany> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTelecomCompany(PathMetadata metadata) {
        super(TelecomCompany.class, metadata);
    }

}

