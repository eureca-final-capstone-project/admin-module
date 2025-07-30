package eureca.capstone.project.admin.report.service;


import eureca.capstone.project.admin.auth.entity.Authority;
import eureca.capstone.project.admin.auth.entity.UserAuthority;
import eureca.capstone.project.admin.auth.repository.AuthorityRepository;
import eureca.capstone.project.admin.auth.repository.UserAuthorityRepository;
import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.exception.custom.AlreadyProcessedRestrictionException;
import eureca.capstone.project.admin.common.exception.custom.RestrictionTargetNotFoundException;
import eureca.capstone.project.admin.common.repository.StatusRepository;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.report.dto.response.RestrictionDto;
import eureca.capstone.project.admin.report.dto.response.RestrictionReportResponseDto;
import eureca.capstone.project.admin.report.entity.ReportHistory;
import eureca.capstone.project.admin.report.entity.ReportType;
import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import eureca.capstone.project.admin.report.entity.RestrictionType;
import eureca.capstone.project.admin.report.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.report.repository.RestrictionAuthorityRepository;
import eureca.capstone.project.admin.report.repository.RestrictionTargetRepository;
import eureca.capstone.project.admin.report.repository.RestrictionTypeRepository;
import eureca.capstone.project.admin.report.service.impl.RestrictionServiceImpl;
import eureca.capstone.project.admin.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.admin.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.admin.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestrictionServiceTest {


    @Mock
    private ReportHistoryRepository reportHistoryRepository;

    @Mock
    private RestrictionTargetRepository restrictionTargetRepository;

    @Mock
    private RestrictionAuthorityRepository restrictionAuthorityRepository;

    @Mock
    private UserAuthorityRepository userAuthorityRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private TransactionFeedRepository transactionFeedRepository;

    @Mock
    private StatusManager statusManager;

    @InjectMocks
    private RestrictionServiceImpl restrictionService;

    private Pageable pageable;
    private ReportHistory report1;
    private ReportHistory report2;
    private RestrictionTarget restriction1;
    private RestrictionTarget restriction2;
    private User user1;
    private User user2;
    private TransactionFeed transactionFeed1;
    private TransactionFeed transactionFeed2;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10); // 페이지 번호 0, 사이즈 10
        user1 = User.builder().userId(100L).email("reporter@example.com").nickname("신고자1").build();
        transactionFeed1 = TransactionFeed.builder().build();
        user2 = User.builder().userId(101L).build();
        transactionFeed2 = TransactionFeed.builder().build();

        report1 = ReportHistory.builder()
                .user(user1)
                .seller(user2)
                .transactionFeed(transactionFeed1)
                .reason("욕설 및 비속어 포함")
                .reportType(ReportType.builder().reportTypeId(1L).type("욕설 및 비속어 포함").build())
                .status(Status.builder().statusId(25L).code("PENDING").description("검수 대기중").build()) // 상태 Enum 사용
                .isModerated(false)
                .build();

        ReflectionTestUtils.setField(report1, "reportHistoryId",1L);
        ReflectionTestUtils.setField(report1, "createdAt",LocalDateTime.now());

        report2 = ReportHistory.builder()
                .user(user2)
                .seller(user1)
                .transactionFeed(transactionFeed2)
                .reason("주제 관련 없음")
                .reportType(ReportType.builder().reportTypeId(2L).type("주제 관련 없음").build())
                .status(Status.builder().statusId(26L).code("AI_ACCEPTED").description("AI 승인").build()) // 상태 Enum 사용
                .isModerated(true)
                .build();

        ReflectionTestUtils.setField(report2, "reportHistoryId",2L);
        ReflectionTestUtils.setField(report2, "createdAt",LocalDateTime.now());

        restriction1 = RestrictionTarget.builder()
                .user(user1)
                .reportType(ReportType.builder().reportTypeId(1L).type("욕설 및 비속어 포함").build())
                .restrictionType(RestrictionType.builder().content("게시글 작성 제한(7일)").duration(7).build())
                .status(Status.builder().code("PENDING").description("제재 대기중").build())
                .build();

        restriction2 = RestrictionTarget.builder()
                .user(user2)
                .reportType(ReportType.builder().reportTypeId(2L).type("주제 관련 없음").build())
                .restrictionType(RestrictionType.builder().content("게시글 작성 제한(1일)").duration(1).build())
                .status(Status.builder().code("COMPLETED").description("제재 완료").build())
                .build();
    }


    @Test
    @DisplayName("제재 내역 목록 조회_조건 없음_성공")
    void getRestrictionList_all_Success() {
        // given
        String statusCode = null;
        String keyword = null;
        Page<RestrictionTarget> page = new PageImpl<>(List.of(restriction1, restriction2));

        when(restrictionTargetRepository.findByCriteria(statusCode, keyword, pageable)).thenReturn(page);

        // when
        Page<RestrictionDto> result = restrictionService.getRestrictionListByStatusCode(statusCode,keyword, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(restrictionTargetRepository).findByCriteria(statusCode, keyword, pageable);
    }

    @Test
    @DisplayName("제재 내역 목록 조회_상태코드로 필터링_성공")
    void getRestrictionList_filtered_Success() {
        // given
        String statusCode = "PENDING";
        String keyword = null;
        Page<RestrictionTarget> page = new PageImpl<>(List.of(restriction1));

        when(restrictionTargetRepository.findByCriteria(statusCode,keyword, pageable)).thenReturn(page);

        // when
        Page<RestrictionDto> result = restrictionService.getRestrictionListByStatusCode(statusCode, keyword, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("PENDING", result.getContent().get(0).getStatus());
        verify(restrictionTargetRepository).findByCriteria(statusCode,keyword, pageable);
    }

    @Test
    @DisplayName("제재 내역 목록 조회_키워드로 검색_성공")
    void getRestrictionList_SearchByKeyword_Success() {
        // given
        String statusCode = null;
        String keyword = "reporter";
        Page<RestrictionTarget> page = new PageImpl<>(List.of(restriction1));

        when(restrictionTargetRepository.findByCriteria(statusCode,keyword, pageable)).thenReturn(page);

        // when
        Page<RestrictionDto> result = restrictionService.getRestrictionListByStatusCode(statusCode, keyword, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(user1.getEmail(), result.getContent().get(0).getUserEmail());
     
        verify(restrictionTargetRepository).findByCriteria(statusCode, keyword, pageable);
    }


    @DisplayName("제재 승인_영구정지일 경우 사용자 상태만 변경")
    @Test
    void acceptRestrictions_Restriction_Success() {
        // given
        Status pendingStatus = Status.builder().statusId(5L).domain("RESTRICTION").code("PENDING").build();
        RestrictionTarget restrictionTarget = RestrictionTarget.builder()
                .restrictionType(RestrictionType.builder().duration(-1).build())
                .status(pendingStatus)
                .user(user1)
                .build();

        ReflectionTestUtils.setField(restrictionTarget, "restrictionTargetId", 1L);

        Status bannedStatus = Status.builder().statusId(1L).domain("USER").code("BANNED").build();
        Status completedStatus = Status.builder().statusId(2L).domain("RESTRICTION").code("COMPLETED").build();
        Status reportCompletedStatus = Status.builder().statusId(3L).domain("REPORT").code("COMPLETED").build();
        Status blurredStatus = Status.builder().statusId(4L).domain("FEED").code("BLURRED").build();

        when(statusManager.getStatus("USER", "BANNED")).thenReturn(bannedStatus);
        when(statusManager.getStatus("RESTRICTION", "COMPLETED")).thenReturn(completedStatus);
        when(statusManager.getStatus("REPORT", "COMPLETED")).thenReturn(reportCompletedStatus);
        when(statusManager.getStatus("FEED", "BLURRED")).thenReturn(blurredStatus);
        when(restrictionTargetRepository.findById(1L)).thenReturn(Optional.of(restrictionTarget));
        when(reportHistoryRepository.findByRestrictionTarget(restrictionTarget)).thenReturn(List.of(report1));

        // when
        restrictionService.acceptRestrictions(1L);

        // then
        assertEquals(bannedStatus.getStatusId(), restrictionTarget.getUser().getStatus().getStatusId());
        assertNull(restrictionTarget.getExpiresAt());
        assertEquals(completedStatus.getStatusId(), restrictionTarget.getStatus().getStatusId());
        assertEquals(reportCompletedStatus.getStatusId(), report1.getStatus().getStatusId());
        assertEquals(blurredStatus.getStatusId(), report1.getTransactionFeed().getStatus().getStatusId());
        verify(transactionFeedRepository).saveAll(anyList());
    }

    @DisplayName("제재 승인_userAuthority에 제재 권한 내역 없는 경우 신규 등록")
    @Test
    void acceptRestrictions_Restriction_newUserAuthority_success() {
        // given
        Status pendingStatus = Status.builder().domain("RESTRICTION").code("PENDING").build();
        Authority authority = Authority.builder().name("WRITE").build();
        ReflectionTestUtils.setField(authority, "authority_id", 1L);

        RestrictionType restrictionType = RestrictionType.builder()
                .restrictionTypeId(1L)
                .duration(7)
                .build();

        RestrictionTarget restrictionTarget = RestrictionTarget.builder()
                .restrictionType(restrictionType)
                .status(pendingStatus)
                .user(user1)
                .build();
        ReflectionTestUtils.setField(restrictionTarget, "restrictionTargetId", 1L);

        Status completedStatus = Status.builder().domain("RESTRICTION").code("COMPLETED").build();
        Status reportCompletedStatus = Status.builder().domain("REPORT").code("COMPLETED").build();
        Status blurredStatus = Status.builder().domain("FEED").code("BLURRED").build();

        when(restrictionTargetRepository.findById(1L)).thenReturn(Optional.of(restrictionTarget));
        when(userAuthorityRepository.findByUserAndAuthority(user1, authority)).thenReturn(null);
        when(statusManager.getStatus("RESTRICTION", "COMPLETED")).thenReturn(completedStatus);
        when(statusManager.getStatus("REPORT", "COMPLETED")).thenReturn(reportCompletedStatus);
        when(statusManager.getStatus("FEED", "BLURRED")).thenReturn(blurredStatus);
        when(reportHistoryRepository.findByRestrictionTarget(restrictionTarget)).thenReturn(List.of(report1));
        when(restrictionAuthorityRepository.findAuthoritiesByRestrictionTypeId(1L)).thenReturn(List.of(authority));

        // when
        restrictionService.acceptRestrictions(1L);

        // then
        verify(userAuthorityRepository).save(any(UserAuthority.class));
        assertNotNull(restrictionTarget.getExpiresAt());
        assertEquals(completedStatus, restrictionTarget.getStatus());
        assertEquals(reportCompletedStatus, report1.getStatus());
        assertEquals(blurredStatus, report1.getTransactionFeed().getStatus());
        verify(transactionFeedRepository).saveAll(anyList());
    }

    @DisplayName("제재 승인_userAuthority에 제재 권한 내역 없는 경우 기존 권한 제재 연장")
    @Test
    void acceptRestrictions_Restriction_extendUserAuthority_success() {
        // given
        Status pendingStatus = Status.builder().domain("RESTRICTION").code("PENDING").build();
        Authority authority = Authority.builder().name("WRITE").build();
        RestrictionType restrictionType = RestrictionType.builder()
                .restrictionTypeId(1L)
                .duration(7)
                .build();
        ReflectionTestUtils.setField(authority, "authority_id", 1L);

        LocalDateTime previousExpiry = LocalDateTime.now();
        UserAuthority userAuthority = UserAuthority.builder()
                .user(user1)
                .authority(authority)
                .expiredAt(previousExpiry)
                .build();

        RestrictionTarget restrictionTarget = RestrictionTarget.builder()
                .restrictionType(restrictionType)
                .status(pendingStatus)
                .user(user1)
                .build();
        ReflectionTestUtils.setField(restrictionTarget, "restrictionTargetId", 1L);

        Status completedStatus = Status.builder().domain("RESTRICTION").code("COMPLETED").build();
        Status reportCompletedStatus = Status.builder().domain("REPORT").code("COMPLETED").build();
        Status blurredStatus = Status.builder().domain("FEED").code("BLURRED").build();

        when(restrictionTargetRepository.findById(1L)).thenReturn(Optional.of(restrictionTarget));
        when(userAuthorityRepository.findByUserAndAuthority(user1, authority)).thenReturn(userAuthority);
        when(statusManager.getStatus("RESTRICTION", "COMPLETED")).thenReturn(completedStatus);
        when(statusManager.getStatus("REPORT", "COMPLETED")).thenReturn(reportCompletedStatus);
        when(statusManager.getStatus("FEED", "BLURRED")).thenReturn(blurredStatus);
        when(reportHistoryRepository.findByRestrictionTarget(restrictionTarget)).thenReturn(List.of(report1));
        when(restrictionAuthorityRepository.findAuthoritiesByRestrictionTypeId(1L)).thenReturn(List.of(authority));

        // when
        restrictionService.acceptRestrictions(1L);

        // then
        assertTrue(userAuthority.getExpiredAt().isAfter(previousExpiry));
        assertEquals(completedStatus, restrictionTarget.getStatus());
        assertEquals(reportCompletedStatus, report1.getStatus());
        assertEquals(blurredStatus, report1.getTransactionFeed().getStatus());
        verify(transactionFeedRepository).saveAll(anyList());
    }

    @DisplayName("제재 승인_실패(AlreadyProcessedRestriction)")
    @Test
    void acceptRestrictions_Fail_AlreadyProcessedRestriction() {
        // given
        Status acceptedStatus = Status.builder().domain("RESTRICTION").code("COMPLETED").build();
        RestrictionType dummyRestrictionType = RestrictionType.builder()
                .duration(7)
                .build();
        RestrictionTarget restrictionTarget = RestrictionTarget.builder()
                .user(user1)
                .status(acceptedStatus)
                .restrictionType(dummyRestrictionType)
                .build();
        ReflectionTestUtils.setField(restrictionTarget, "restrictionTargetId", 1L);


        when(restrictionTargetRepository.findById(1L)).thenReturn(Optional.of(restrictionTarget));
        when(statusManager.getStatus("RESTRICTION", "COMPLETED")).thenReturn(acceptedStatus);

        // when & then
        assertThrows(AlreadyProcessedRestrictionException.class,
                () -> restrictionService.acceptRestrictions(1L));
    }

    @DisplayName("제재 거절_성공")
    @Test
    void rejectRestrictions_success() {
        // given
        Status acceptedStatus = Status.builder().domain("RESTRICTION").code("COMPLETED").build();
        Status rejectedStatus = Status.builder().domain("RESTRICTION").code("REJECTED").build();

        RestrictionTarget restrictionTarget = RestrictionTarget.builder()
                .user(user1)
                .status(acceptedStatus)
                .build();
        ReflectionTestUtils.setField(restrictionTarget, "restrictionTargetId", 1L);

        when(restrictionTargetRepository.findById(1L)).thenReturn(Optional.of(restrictionTarget));
        when(statusManager.getStatus("RESTRICTION", "REJECTED"))
                .thenReturn(rejectedStatus);
        // when
        restrictionService.rejectRestrictions(1L);

        // then
        assertEquals(rejectedStatus, restrictionTarget.getStatus());
    }

    @DisplayName("제재 거절_실패(AlreadyProcessedRestriction)")
    @Test
    void rejectRestrictions_Fail_AlreadyProcessedRestriction() {
        // given
        Status rejectedStatus = Status.builder().domain("RESTRICTION").code("REJECTED").build();

        RestrictionTarget restrictionTarget = RestrictionTarget.builder()
                .user(user1)
                .status(rejectedStatus)
                .build();
        ReflectionTestUtils.setField(restrictionTarget, "restrictionTargetId", 1L);

        when(restrictionTargetRepository.findById(1L)).thenReturn(Optional.of(restrictionTarget));
        when(statusManager.getStatus("RESTRICTION", "REJECTED"))
                .thenReturn(rejectedStatus);

        // when & then
        assertThrows(AlreadyProcessedRestrictionException.class,
                () -> restrictionService.rejectRestrictions(1L));
    }



    @DisplayName("제재 id로 신고내역 조회_성공")
    @Test
    void getRestrictionReportHistory_Success() {
        // given
        Long restrictionId = 1L;

        RestrictionTarget restrictionTarget = RestrictionTarget.builder().build();
        ReflectionTestUtils.setField(restrictionTarget, "restrictionTargetId",restrictionId);

        List<RestrictionReportResponseDto> mockResponse = List.of(
                new RestrictionReportResponseDto(1L, "욕설 및 비속어 포함", "욕설입니다", LocalDateTime.now(), "AI 승인"),
                new RestrictionReportResponseDto(2L, "욕설 및 비속어 포함", "또 욕설", LocalDateTime.now(), "관리자 승인")
        );

        when(restrictionTargetRepository.existsById(restrictionId)).thenReturn(true);

        when(reportHistoryRepository.getRestrictionReportList(restrictionId))
                .thenReturn(mockResponse);

        // when
        List<RestrictionReportResponseDto> result = restrictionService.getRestrictionReportHistory(restrictionId);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getReportId()).isEqualTo(1L);
        assertThat(result.get(1).getStatus()).isEqualTo("관리자 승인");

        verify(restrictionTargetRepository).existsById(restrictionId);
        verify(reportHistoryRepository).getRestrictionReportList(restrictionId);
    }

    @DisplayName("제재 id로 신고내역 조회_RestrictionNotFound")
    @Test
    void getRestrictionReportHistory_RestrictionNotFound() {
        // given
        Long restrictionId = 99L;

        when(restrictionTargetRepository.existsById(restrictionId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> restrictionService.getRestrictionReportHistory(restrictionId))
                .isInstanceOf(RestrictionTargetNotFoundException.class);

        verify(restrictionTargetRepository).existsById(restrictionId);
        verify(reportHistoryRepository, never()).getRestrictionReportList(any());
    }
}
