package eureca.capstone.project.admin.report.service.impl;

import eureca.capstone.project.admin.auth.entity.Authority;
import eureca.capstone.project.admin.auth.entity.UserAuthority;
import eureca.capstone.project.admin.auth.repository.AuthorityRepository;
import eureca.capstone.project.admin.auth.repository.UserAuthorityRepository;
import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.exception.custom.AlreadyProcessedRestrictionException;
import eureca.capstone.project.admin.common.exception.custom.RestrictionTargetNotFoundException;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.report.dto.response.RestrictionDto;
import eureca.capstone.project.admin.report.dto.response.RestrictionReportResponseDto;
import eureca.capstone.project.admin.report.entity.ReportHistory;
import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import eureca.capstone.project.admin.report.entity.RestrictionType;
import eureca.capstone.project.admin.report.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.report.repository.ReportTypeRepository;
import eureca.capstone.project.admin.report.repository.RestrictionTargetRepository;
import eureca.capstone.project.admin.report.repository.RestrictionTypeRepository;
import eureca.capstone.project.admin.report.service.RestrictionService;
import eureca.capstone.project.admin.report.service.external.AIReviewService;
import eureca.capstone.project.admin.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.admin.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.admin.user.entity.User;
import eureca.capstone.project.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static eureca.capstone.project.admin.common.entity.StatusConst.FEED;
import static eureca.capstone.project.admin.common.entity.StatusConst.RESTRICTION;
import static eureca.capstone.project.admin.common.constant.RestrictionConst.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestrictionServiceImpl implements RestrictionService {
    private final ReportHistoryRepository reportHistoryRepository;
    private final RestrictionTargetRepository restrictionTargetRepository;
    private final TransactionFeedRepository transactionFeedRepository;
    private final StatusManager statusManager;
    private final UserAuthorityRepository userAuthorityRepository;
    private final AuthorityRepository authorityRepository;

    @Override
    public Page<RestrictionDto> getRestrictionListByStatusCode(String statusCode,String keyword, Pageable pageable) {
        Page<RestrictionTarget> restrictionTarget = restrictionTargetRepository.findByCriteria(statusCode, keyword, pageable);
        log.info("[getRestrictionListByStatusCode] 신고 내역 조회 (keyword: {}, statusCode: {}): 총 {} 건", keyword, statusCode, restrictionTarget.getTotalElements());
        return restrictionTarget.map(RestrictionDto::from);
    }


    @Transactional
    @Override
    public void acceptRestrictions(Long restrictionTargetId) {
        RestrictionTarget restrictionTarget = restrictionTargetRepository.findById(restrictionTargetId)
                .orElseThrow(RestrictionTargetNotFoundException::new);

        Status completedStatus = statusManager.getStatus(RESTRICTION, "COMPLETED");

        if (restrictionTarget.getStatus().equals(completedStatus)) {
            throw new AlreadyProcessedRestrictionException();
        }

        User user = restrictionTarget.getUser();
        RestrictionType restrictionType = restrictionTarget.getRestrictionType();
        Integer duration = restrictionTarget.getRestrictionType().getDuration();

        log.info("[acceptRestrictions] 제재대상 및 기간 조회. 사용자: {}, 제재타입: {}, 제재기간: {} (-1은 영구정지)", user.getUserId(), restrictionTarget.getRestrictionType().getContent(), duration);

        // 제재 만료일
        LocalDateTime expiresAt;

        if (duration == -1) { // 영구정지일 경우 사용자 block
            expiresAt = null;
            user.updateUserStatus(statusManager.getStatus("USER", "BANNED"));
            log.info("[acceptRestrictions] 영구정지 user 상태 변경: {}", user.getStatus().getCode());
        } else {
            List<Authority> authoritiesToRestrict = resolveAuthorities(restrictionType);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime finalExpiresAt = null;

            // 제재해야 할 권한 순회하면서 권한제재 진행
            for (Authority authority : authoritiesToRestrict) {
                UserAuthority userAuthority = userAuthorityRepository.findByUserAndAuthority(user, authority);
                log.info("[acceptRestrictions] userAuthority에서 권한 조회결과: {}", userAuthority);

                LocalDateTime newExpireAt;

                // 해당 권한에 대한 제재내역이 없는 경우
                if (userAuthority == null) {
                    newExpireAt = now.plusDays(duration);
                    userAuthorityRepository.save(
                            UserAuthority.builder()
                                    .user(user)
                                    .authority(authority)
                                    .expiredAt(newExpireAt)
                                    .build());
                    log.info("[acceptRestrictions] 해당 권한에 대한 제재내역 없음 => expiresAt: {} ", newExpireAt);
                }
                // 해당 권한에 대해 이미 제재받은 경우
                else {
                    newExpireAt = userAuthority.getExpiredAt().plusDays(duration);
                    userAuthority.updateExpiresAt(newExpireAt);
                    log.info("[acceptRestrictions] 해당 권한에 대한 제재내역 있음 => expiresAt: {} ", newExpireAt);
                }

                // 최종 저장할 만료일자 판별
                if (finalExpiresAt == null || finalExpiresAt.isBefore(newExpireAt)) {
                    finalExpiresAt = newExpireAt;
                }
            }
            expiresAt = finalExpiresAt;
            restrictionTarget.updateExpiresAt(expiresAt);
            restrictionTarget.updateStatus(statusManager.getStatus("RESTRICTION", "COMPLETED"));
            log.info("[acceptRestrictions] 제재 완료. expiresAt: {}, status: {}", expiresAt, restrictionTarget.getStatus().getCode());

            //제재와 연관된 신고 내역들의 상태를 '제재 완료'로 변경
            Status reportCompletedStatus = statusManager.getStatus("REPORT", "COMPLETED");
            List<ReportHistory> relatedReports = reportHistoryRepository.findByRestrictionTarget(restrictionTarget);
            relatedReports.forEach(report -> report.updateStatus(reportCompletedStatus));
            reportHistoryRepository.saveAll(relatedReports);
            log.info("[acceptRestrictions] 연관된 신고 내역 {}건의 상태를 '제재 완료'로 변경했습니다.", relatedReports.size());

            // 제재와 연관된 게시글의 상태를 BLURRED로 변경
            Status blurredStatus = statusManager.getStatus(FEED, "BLURRED");
            List<TransactionFeed> transactionFeedsToBlur = relatedReports.stream()
                    .map(ReportHistory::getTransactionFeed)
                    .distinct() // 중복 게시글 제거
                    .toList();
            transactionFeedsToBlur.forEach(feed -> feed.updateStatus(blurredStatus));
            transactionFeedRepository.saveAll(transactionFeedsToBlur);
            log.info("[acceptRestrictions] 연관된 게시글 {}건의 상태를 'BLURRED'로 변경했습니다.", transactionFeedsToBlur.size());
        }
    }

    @Transactional
    @Override
    public void rejectRestrictions(Long restrictionTargetId) {
        RestrictionTarget restrictionTarget = restrictionTargetRepository.findById(restrictionTargetId)
                .orElseThrow(RestrictionTargetNotFoundException::new);

        Status rejectedStatus = statusManager.getStatus(RESTRICTION, "REJECTED");

        if(restrictionTarget.getStatus().equals(rejectedStatus)) {
            throw new AlreadyProcessedRestrictionException();
        }

        restrictionTarget.updateStatus(statusManager.getStatus("RESTRICTION", "REJECTED"));
        log.info("[acceptRestrictions] 제재 거절 완료. status: {}", restrictionTarget.getStatus().getCode());

        Status reportRejectedStatus = statusManager.getStatus("REPORT", "REJECTED");
        List<ReportHistory> relatedReports = reportHistoryRepository.findByRestrictionTarget(restrictionTarget);
        relatedReports.forEach(report -> report.updateStatus(reportRejectedStatus));
        reportHistoryRepository.saveAll(relatedReports);
        log.info("[rejectRestrictions] 연관된 신고 내역 {}건의 상태를 '제재 미승인'으로 변경했습니다.", relatedReports.size());
    }


    @Override
    public List<RestrictionReportResponseDto> getRestrictionReportHistory(Long restrictionId) {

        if(!restrictionTargetRepository.existsById(restrictionId)) {
            throw new RestrictionTargetNotFoundException();
        }

        List<RestrictionReportResponseDto> response = reportHistoryRepository.getRestrictionReportList(restrictionId);

        log.info("[getRestrictionReportHistory] 제재와 연관된 신고내역 조회: 총 {} 건", response.size());

        return response;
    }


    // 제한해야 할 권한 리스트
    private List<Authority> resolveAuthorities(RestrictionType restrictionType) {
        List<String> authorities = RTYPE_TO_AUTHORITIES.getOrDefault(
                restrictionType.getRestrictionTypeId(),
                restrictionType.getAuthority() == null ? List.of() : List.of(restrictionType.getAuthority().getName()));

        return authorityRepository.findByNameIn(authorities);
    }
}
