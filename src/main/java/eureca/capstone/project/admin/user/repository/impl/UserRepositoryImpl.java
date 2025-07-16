package eureca.capstone.project.admin.user.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import eureca.capstone.project.admin.user.repository.custom.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static eureca.capstone.project.admin.user.entity.QUser.user;
import static eureca.capstone.project.admin.report.entity.QReportHistory.reportHistory;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

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
}
