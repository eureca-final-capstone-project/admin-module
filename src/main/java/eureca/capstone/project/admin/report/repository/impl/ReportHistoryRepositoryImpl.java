package eureca.capstone.project.admin.report.repository.impl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.admin.report.dto.response.ReportDetailResponseDto;
import eureca.capstone.project.admin.report.entity.QReportHistory;
import eureca.capstone.project.admin.report.repository.custom.ReportHistoryRepositoryCustom;
import eureca.capstone.project.admin.transaction_feed.entity.QTransactionFeed;
import eureca.capstone.project.admin.user.dto.UserInformationDto;
import eureca.capstone.project.admin.user.dto.response.UserReportResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import eureca.capstone.project.admin.user.entity.QUser;
import eureca.capstone.project.admin.user.repository.custom.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static eureca.capstone.project.admin.auth.entity.QAuthority.authority;
import static eureca.capstone.project.admin.auth.entity.QRole.role;
import static eureca.capstone.project.admin.auth.entity.QRoleAuthority.roleAuthority;
import static eureca.capstone.project.admin.auth.entity.QUserRole.userRole;
import static eureca.capstone.project.admin.report.entity.QReportHistory.reportHistory;
import static eureca.capstone.project.admin.transaction_feed.entity.QTransactionFeed.transactionFeed;
import static eureca.capstone.project.admin.user.entity.QUser.user;

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
