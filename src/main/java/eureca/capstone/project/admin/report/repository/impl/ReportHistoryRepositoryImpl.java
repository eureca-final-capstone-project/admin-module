package eureca.capstone.project.admin.report.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.admin.report.dto.response.ReportDetailResponseDto;
import eureca.capstone.project.admin.report.repository.custom.ReportHistoryRepositoryCustom;
import eureca.capstone.project.admin.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;


import static eureca.capstone.project.admin.report.entity.QReportHistory.reportHistory;
import static eureca.capstone.project.admin.transaction_feed.entity.QTransactionFeed.transactionFeed;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReportHistoryRepositoryImpl implements ReportHistoryRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

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
}
