package eureca.capstone.project.admin.report.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.admin.report.dto.response.RestrictionReportResponseDto;
import eureca.capstone.project.admin.report.entity.ReportHistory;
import eureca.capstone.project.admin.report.dto.response.ReportDetailResponseDto;
import eureca.capstone.project.admin.report.repository.custom.ReportHistoryRepositoryCustom;
import eureca.capstone.project.admin.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static eureca.capstone.project.admin.report.entity.QReportHistory.reportHistory;
import static eureca.capstone.project.admin.transaction_feed.entity.QTransactionFeed.transactionFeed;
import static eureca.capstone.project.admin.user.entity.QUser.user;

@Slf4j
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

    @Override
    public ReportDetailResponseDto getReportDetail(Long id) {
        log.info("[getReportDetail] 시작: {}", id);

        QUser reporter = new QUser("reporter");
        QUser seller = new QUser("seller");

        ReportDetailResponseDto reportDetail = jpaQueryFactory
                .select(Projections.constructor(ReportDetailResponseDto.class,
                        reportHistory.reportHistoryId,
                        reportHistory.status.description,
                        reporter.email,
                        reportHistory.createdAt,
                        reportHistory.reportType.type,
                        reportHistory.reason,
                        transactionFeed.telecomCompany.name,
                        transactionFeed.salesDataAmount,
                        transactionFeed.title,
                        transactionFeed.content,
                        seller.email,
                        transactionFeed.createdAt,
                        transactionFeed.salesPrice
                ))
                .from(reportHistory)
                .join(reportHistory.user, reporter)
                .join(reportHistory.transactionFeed, transactionFeed)
                .join(reportHistory.seller, seller)
                .where(reportHistory.reportHistoryId.eq(id))
                .fetchOne();

        log.info("[getReportDetail] 쿼리 생성: {}", reportDetail.getReportId());

        return reportDetail;
    }

    @Override
    public List<RestrictionReportResponseDto> getRestrictionReportList(Long restrictionId) {

        List<RestrictionReportResponseDto> reportList = jpaQueryFactory
                .select(Projections.constructor(RestrictionReportResponseDto.class,
                        reportHistory.reportHistoryId,
                        reportHistory.reportType.explanation,
                        transactionFeed.content,
                        reportHistory.createdAt,
                        reportHistory.status.description
                        ))
                .from(reportHistory)
                .join(reportHistory.transactionFeed, transactionFeed)
                .where(reportHistory.restrictionTarget.restrictionTargetId.eq(restrictionId))
                .fetch();

        log.info("[getRestrictionReportList] 쿼리 생성: 총 {}건", reportList.size());
        return reportList;
    }

    private BooleanExpression statusEquals(String statusCode) {
        return StringUtils.hasText(statusCode) ? reportHistory.status.code.eq(statusCode) : null;
    }

    private BooleanExpression reporterEmailContains(String keyword) {
        return StringUtils.hasText(keyword) ? user.email.containsIgnoreCase(keyword) : null;
    }
}
