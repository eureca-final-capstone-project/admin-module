package eureca.capstone.project.admin.report.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import eureca.capstone.project.admin.report.repository.custom.RestrictionTargetRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static eureca.capstone.project.admin.common.entity.QStatus.status;
import static eureca.capstone.project.admin.report.entity.QReportType.reportType;
import static eureca.capstone.project.admin.report.entity.QRestrictionTarget.restrictionTarget;
import static eureca.capstone.project.admin.report.entity.QRestrictionType.restrictionType;
import static eureca.capstone.project.admin.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class RestrictionTargetRepositoryImpl implements RestrictionTargetRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<RestrictionTarget> findByCriteria(String statusCode, String keyword, Pageable pageable) {

        List<RestrictionTarget> content = jpaQueryFactory
                .selectFrom(restrictionTarget)
                .join(restrictionTarget.user, user).fetchJoin()
                .join(restrictionTarget.status, status).fetchJoin()
                .join(restrictionTarget.reportType, reportType).fetchJoin()
                .join(restrictionTarget.restrictionType, restrictionType).fetchJoin()
                .where(
                        statusEquals(statusCode),
                        userEmailContains(keyword)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(restrictionTarget.count())
                .from(restrictionTarget)
                .join(restrictionTarget.user, user)
                .join(restrictionTarget.status, status)
                .where(
                        statusEquals(statusCode),
                        userEmailContains(keyword)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEquals(String statusCode) {
        return StringUtils.hasText(statusCode) ? restrictionTarget.status.code.eq(statusCode) : null;
    }

    private BooleanExpression userEmailContains(String keyword) {
        return StringUtils.hasText(keyword) ? restrictionTarget.user.email.containsIgnoreCase(keyword) : null;
    }
}