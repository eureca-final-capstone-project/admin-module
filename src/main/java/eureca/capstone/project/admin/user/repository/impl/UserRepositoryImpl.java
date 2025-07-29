package eureca.capstone.project.admin.user.repository.impl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.admin.user.dto.UserInformationDto;
import eureca.capstone.project.admin.user.dto.response.MyReportResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserReportResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import eureca.capstone.project.admin.user.repository.custom.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static eureca.capstone.project.admin.user.entity.QUser.user;
import static eureca.capstone.project.admin.report.entity.QReportHistory.reportHistory;
import static eureca.capstone.project.admin.transaction_feed.entity.QTransactionFeed.transactionFeed;
import static eureca.capstone.project.admin.auth.entity.QRole.role;
import static eureca.capstone.project.admin.auth.entity.QRoleAuthority.roleAuthority;
import static eureca.capstone.project.admin.auth.entity.QUserRole.userRole;
import static eureca.capstone.project.admin.auth.entity.QAuthority.authority;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    // 사용자 목록 조회
    @Override
    public Page<UserResponseDto> getUserList(String keyword, Pageable pageable) {
        log.info("[getUserList] 시작: keyword={}, pageable={}", keyword, pageable);

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
                                        .and(reportHistory.status.statusId.in(26, 28, 41)))
                .where(searchCondition(keyword),
                        user.status.statusId.in(12,13))
                .groupBy(user.userId)
                .orderBy(user.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        log.info("[getUserList] 조회 쿼리 실행. size: {}", userList.size());

        JPAQuery<Long> count = jpaQueryFactory
                .select(user.countDistinct())
                .from(user)
                .where(searchCondition(keyword),
                        user.status.statusId.in(12,13));

        return PageableExecutionUtils.getPage(userList, pageable, count::fetchOne);
    }

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
                .where(reportHistory.status.statusId.in(26,28,41)
                        .and(user.userId.eq(userId)))
                .fetch();

        log.info("[getUserReportList] 조회 쿼리 실행. size: {}", reportList.size());

        return reportList;
    }

    @Override
    public UserInformationDto findAdminInformation(String email) {
        log.info("[findUserInformation] 시작 - email: {}", email);

        List<Tuple> result = jpaQueryFactory
                .select(user.userId, user.email, user.password, role.name, authority.name)
                .from(userRole)
                .innerJoin(userRole.user, user)
                .innerJoin(userRole.role, role)
                .innerJoin(roleAuthority).on(roleAuthority.role.eq(role))
                .innerJoin(roleAuthority.authority, authority)
                .where(
                        user.email.eq(email),
                        user.status.code.eq("ACTIVE"),
                        role.name.eq("ROLE_ADMIN")
                ) // email, ACTIVE 기준 필터링
                .fetch();

        log.info("[findUserInformation] 쿼리 실행 결과 size: {}", result.size());

        if (result.isEmpty()) {
            log.info("[findUserInformation] 결과 없음 - 빈 DTO 반환");
            return UserInformationDto.emptyDto();
        }

        Long userId = result.get(0).get(user.userId);
        String password = result.get(0).get(user.password);
        String emailFromDB = result.get(0).get(user.email);
        Set<String> roles = new HashSet<>();
        Set<String> authorities = new HashSet<>();

        for (Tuple tuple : result) {
            String roleName = tuple.get(role.name);
            String authorityName = tuple.get(authority.name);
            roles.add(roleName);
            authorities.add(authorityName);
            log.info("[findUserInformation] row - role: {}, authority: {}", roleName, authorityName);
        }

        UserInformationDto userInformationDto = UserInformationDto.builder()
                .userId(userId)
                .password(password)
                .email(emailFromDB)
                .roles(roles)
                .authorities(authorities)
                .build();

        log.info("[findUserInformation] DTO 생성 완료 {}", userInformationDto);

        return userInformationDto;
    }

    private BooleanExpression searchCondition(String keyword) {
        // keyword가 비어있거나 null이면 null을 반환하여 where절에서 무시됩니다.
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        // email LIKE '%keyword%' OR nickname LIKE '%keyword%' (대소문자 무시)
        return user.email.containsIgnoreCase(keyword)
                .or(user.nickname.containsIgnoreCase(keyword));
    }

    @Override
    public List<MyReportResponseDto> findMyReportList(Long userId) {
        return jpaQueryFactory
                .select(Projections.constructor(MyReportResponseDto.class,
                        transactionFeed.transactionFeedId,
                        transactionFeed.title,
                        transactionFeed.salesDataAmount,
                        reportHistory.reportType.explanation,
                        reportHistory.createdAt,
                        reportHistory.status.description,
                        reportHistory.reason
                ))
                .from(reportHistory)
                .innerJoin(reportHistory.user, user)
                .innerJoin(reportHistory.transactionFeed, transactionFeed)
                .where(user.userId.eq(userId))
                .orderBy(reportHistory.createdAt.desc())
                .fetch();
    }
}
