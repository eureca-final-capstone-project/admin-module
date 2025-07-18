package eureca.capstone.project.admin.report.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.admin.report.entity.ReportHistory;
import eureca.capstone.project.admin.report.repository.custom.ReportHistoryRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static eureca.capstone.project.admin.report.entity.QReportHistory.reportHistory;
import static eureca.capstone.project.admin.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class ReportHistoryRepositoryImpl implements ReportHistoryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ReportHistory> findByCriteria(String statusCode, String keyword, Pageable pageable) {
        List<ReportHistory> content = jpaQueryFactory
                .selectFrom(reportHistory)
                .join(reportHistory.user, user).fetchJoin() // 신고자
                .join(reportHistory.status).fetchJoin()
                .join(reportHistory.reportType).fetchJoin()
                .join(reportHistory.transactionFeed).fetchJoin()
                .where(
                        statusEquals(statusCode),
                        reporterEmailContains(keyword)
                )
                .orderBy(reportHistory.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(reportHistory.count())
                .from(reportHistory)
                .join(reportHistory.user, user) // 조건절에 사용되므로 join 필요
                .where(statusEquals(statusCode), reporterEmailContains(keyword));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEquals(String statusCode) {
        return StringUtils.hasText(statusCode) ? reportHistory.status.code.eq(statusCode) : null;
    }

    private BooleanExpression reporterEmailContains(String keyword) {
        return StringUtils.hasText(keyword) ? user.email.containsIgnoreCase(keyword) : null;
    }
}