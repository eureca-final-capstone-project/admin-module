package eureca.capstone.project.admin.user.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.admin.user.dto.response.UserReportResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import eureca.capstone.project.admin.user.repository.custom.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static eureca.capstone.project.admin.user.entity.QUser.user;
import static eureca.capstone.project.admin.report.entity.QReportHistory.reportHistory;
import static eureca.capstone.project.admin.transaction_feed.entity.QTransactionFeed.transactionFeed;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    // 사용자 목록 조회
    @Override
    public Page<UserResponseDto> getUserList(Pageable pageable) {
        log.info("[getUserList] 시작: {}", pageable);

        List<UserResponseDto> userList = jpaQueryFactory
                .select(Projections.constructor(UserResponseDto.class,
                        user.userId,
                        user.email,
                        user.nickname,
                        user.telecomCompany.name,
                        user.phoneNumber,
                        user.createdAt,
                        user.status.description,
                        reportHistory.reportHistoryId.count()
                        ))
                .from(user)
                .leftJoin(reportHistory).on(reportHistory.seller.eq(user)
                                        .and(reportHistory.status.statusId.in(26,28)))
                .groupBy(user.userId)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        log.info("[getUserList] 조회 쿼리 실행. size: {}", userList.size());

        JPAQuery<Long> count = jpaQueryFactory
                .select(user.countDistinct())
                .from(user);

        return PageableExecutionUtils.getPage(userList, pageable, count::fetchOne);
    }

    // TODO : 허위신고 구현 방법에 따라 다시 검토
    @Override
    public List<UserReportResponseDto> getUserReportList(Long userId) {

        List<UserReportResponseDto> reportList = jpaQueryFactory
                .select(Projections.constructor(UserReportResponseDto.class,
                        reportHistory.reportHistoryId,
                        reportHistory.reportType.explanation,
                        transactionFeed.content,
                        transactionFeed.createdAt,
                        reportHistory.status.description
                        ))
                .from(reportHistory)
                .innerJoin(reportHistory.seller, user)
                .innerJoin(transactionFeed).on(reportHistory.transactionFeed.eq(transactionFeed))
                .where(reportHistory.status.statusId.in(26,28)
                        .and(user.userId.eq(userId)))
                .fetch();

        log.info("[getUserReportList] 조회 쿼리 실행. size: {}", reportList.size());

        return reportList;
    }

}
