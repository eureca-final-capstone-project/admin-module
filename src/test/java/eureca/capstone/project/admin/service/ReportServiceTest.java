package eureca.capstone.project.admin.service;

import eureca.capstone.project.admin.report.entity.ReportHistory;
import eureca.capstone.project.admin.report.entity.ReportType;
import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import eureca.capstone.project.admin.report.entity.RestrictionType;
import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.admin.user.entity.User;
import eureca.capstone.project.admin.report.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.report.dto.response.ReportCountDto;
import eureca.capstone.project.admin.report.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.report.dto.response.RestrictExpiredResponseDto;
import eureca.capstone.project.admin.report.dto.response.RestrictionDto;
import eureca.capstone.project.admin.common.exception.custom.AlreadyProcessedReportException;
import eureca.capstone.project.admin.report.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.report.repository.RestrictionTargetRepository;
import eureca.capstone.project.admin.report.repository.RestrictionTypeRepository;
import eureca.capstone.project.admin.common.repository.StatusRepository;
import eureca.capstone.project.admin.report.service.impl.ReportServiceImpl;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportHistoryRepository reportHistoryRepository;

    @Mock
    private RestrictionTargetRepository restrictionTargetRepository;

    @Mock
    private RestrictionTypeRepository restrictionTypeRepository;

    @Mock
    private StatusRepository statusRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

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
        user1 = User.builder().userId(100L).build();
        transactionFeed1 = TransactionFeed.builder().build();
        user2 = User.builder().userId(101L).build();
        transactionFeed2 = TransactionFeed.builder().build();

        report1 = ReportHistory.builder()
                .user(user1)
                .transactionFeed(transactionFeed1)
                .reason("욕설 및 비속어 포함")
                .reportType(ReportType.builder().reportTypeId(1L).type("욕설 및 비속어 포함").build())
                .status(Status.builder().code("MODERATIN_PENDING").build()) // 상태 Enum 사용
                .isModerated(false)
                .build();

        report2 = ReportHistory.builder()
                .user(user2)
                .transactionFeed(transactionFeed2)
                .reason("주제 관련 없음")
                .reportType(ReportType.builder().reportTypeId(2L).type("주제 관련 없음").build())
                .status(Status.builder().code("AI_ACCEPTED").build()) // 상태 Enum 사용
                .isModerated(true)
                .build();

        restriction1 = RestrictionTarget.builder()
                .user(user1)
                .reportType(ReportType.builder().reportTypeId(1L).type("욕설 및 비속어 포함").build())
                .restrictionType(RestrictionType.builder().content("게시글 작성 제한(7일)").duration(7).build())
                .status(Status.builder().code("PENDING").build())
                .build();

        restriction2 = RestrictionTarget.builder()
                .user(user2)
                .reportType(ReportType.builder().reportTypeId(2L).type("주제 관련 없음").build())
                .restrictionType(RestrictionType.builder().content("게시글 작성 제한(1일)").duration(1).build())
                .status(Status.builder().code("COMPLETED").build())
                .build();
    }

    @Test
    @DisplayName("신고 건수 조회_성공")
    void getReportCounts_Success() {
        // given
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        when(reportHistoryRepository.countByCreatedAtAfter(startOfToday)).thenReturn(5L);
        when(reportHistoryRepository.count()).thenReturn(20L);

        // when
        ReportCountDto result = reportService.getReportCounts();

        // then
        assertNotNull(result);
        assertEquals(5L, result.getTodayReportCount());
        assertEquals(20L, result.getTotalReportCount());

        verify(reportHistoryRepository).countByCreatedAtAfter(startOfToday);
        verify(reportHistoryRepository).count();
    }


    @Test
    @DisplayName("신고 내역 전체 조회_성공")
    void getReportHistory_all_Success() {
        // given
        Page<ReportHistory> page = new PageImpl<>(List.of(report1, report2));
        when(reportHistoryRepository.findAll(pageable)).thenReturn(page);

        // when
        Page<ReportHistoryDto> result = reportService.getReportHistoryListByStatusCode(null, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(report1.getReportHistoryId(), result.getContent().get(0).getReportHistoryId());
        assertEquals(report2.getReportHistoryId(), result.getContent().get(1).getReportHistoryId());
        assertEquals(report1.getUser().getUserId(), result.getContent().get(0).getUserId());
        verify(reportHistoryRepository).findAll(pageable);
    }


    @Test
    @DisplayName("신고 내역 필터링 조회_성공")
    void getReportHistory_filtering_Success() {
        // given
        String statusCode = "PENDING";
        Status pendingStatus = report1.getStatus();
        Page<ReportHistory> page = new PageImpl<>(List.of(report1));

        when(statusRepository.findByDomainAndCode("REPORT", statusCode)).thenReturn(Optional.of(pendingStatus));
        when(reportHistoryRepository.findByStatus(pendingStatus, pageable)).thenReturn(page);

        // when
        Page<ReportHistoryDto> result = reportService.getReportHistoryListByStatusCode(statusCode, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(statusRepository).findByDomainAndCode("REPORT", statusCode);
        verify(reportHistoryRepository).findByStatus(pendingStatus, pageable);
    }

    @Test
    @DisplayName("제재 내역 전체 조회_성공")
    void getRestrictionList_all_Success() {
        Page<RestrictionTarget> page = new PageImpl<>(List.of(restriction1, restriction2));
        when(restrictionTargetRepository.findAll(pageable)).thenReturn(page);

        Page<RestrictionDto> result = reportService.getRestrictionListByStatusCode(null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(restrictionTargetRepository).findAll(pageable);
    }

    @Test
    @DisplayName("제재 내역 상태 필터링 조회_성공")
    void getRestrictionList_filtered_Success() {
        String statusCode = "PENDING";
        Status pendingStatus = restriction1.getStatus();
        Page<RestrictionTarget> page = new PageImpl<>(List.of(restriction1));

        // 변경점: statusRepository.findByDomainAndCode Mocking 추가
        when(statusRepository.findByDomainAndCode("RESTRICTION", statusCode)).thenReturn(Optional.of(pendingStatus));
        when(restrictionTargetRepository.findByStatus(pendingStatus, pageable)).thenReturn(page);

        // 변경점: 새 메서드 호출
        Page<RestrictionDto> result = reportService.getRestrictionListByStatusCode(statusCode, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(statusRepository).findByDomainAndCode("RESTRICTION", statusCode);
        verify(restrictionTargetRepository).findByStatus(pendingStatus, pageable);
    }

    @Test
    @DisplayName("관리자 신고_미승인")
    void processReportByAdmin_reject_Success() {
        // given
        ProcessReportDto request = new ProcessReportDto();
        ReflectionTestUtils.setField(request, "approved", false);
        Status pending = Status.builder().code("MODERATION_PENDING").build();
        Status aiRejected = Status.builder().code("AI_REJECTED").build();
        Status adminRejected = Status.builder().code("ADMIN_REJECTED").build();

        report1.updateStatus(pending);

        when(statusRepository.findByCode("MODERATION_PENDING")).thenReturn(pending);
        when(statusRepository.findByCode("AI_REJECTED")).thenReturn(aiRejected);
        when(statusRepository.findByCode("ADMIN_REJECTED")).thenReturn(adminRejected);
        when(reportHistoryRepository.findById(1L)).thenReturn(Optional.of(report1));

        // when
        reportService.processReportByAdmin(1L, request);

        // then
        assertEquals(adminRejected, report1.getStatus());
        verify(reportHistoryRepository).findById(1L);
    }

    @Test
    @DisplayName("관리자 신고_승인")
    void processReportByAdmin_approve_Success() {
        // given
        ProcessReportDto request = new ProcessReportDto();
        ReflectionTestUtils.setField(request, "approved", true);

        Status pending = Status.builder().code("MODERATION_PENDING").build();
        Status aiAccepted = Status.builder().code("AI_ACCEPTED").build();
        Status aiRejected = Status.builder().code("AI_REJECTED").build();
        Status adminAccepted = Status.builder().code("ADMIN_ACCEPTED").build();

        report1.updateStatus(pending);

        when(reportHistoryRepository.findById(1L)).thenReturn(Optional.of(report1));
        when(statusRepository.findByCode("MODERATION_PENDING")).thenReturn(pending);
        when(statusRepository.findByCode("AI_ACCEPTED")).thenReturn(aiAccepted);
        when(statusRepository.findByCode("AI_REJECTED")).thenReturn(aiRejected);
        when(statusRepository.findByCode("ADMIN_ACCEPTED")).thenReturn(adminAccepted);

        // when
        reportService.processReportByAdmin(1L, request);

        // then
        assertEquals(adminAccepted, report1.getStatus());
        verify(reportHistoryRepository).findById(1L);
    }

    @Test
    @DisplayName("관리자 신고_이미 처리됨 예외")
    void processReportByAdmin_alreadyProcessed_exception() {
        // given
        ProcessReportDto request = new ProcessReportDto();
        ReflectionTestUtils.setField(request, "approved", true);

        when(statusRepository.findByCode("MODERATION_PENDING")).thenReturn(Status.builder().code("MODERATION_PENDING").build());
        when(statusRepository.findByCode("AI_REJECTED")).thenReturn(Status.builder().code("AI_REJECTED").build());
        when(reportHistoryRepository.findById(2L)).thenReturn(Optional.of(report2));

        // then
        assertThrows(AlreadyProcessedReportException.class, () ->
                reportService.processReportByAdmin(2L, request)
        );

        verify(reportHistoryRepository).findById(2L);
    }


    @Test
    @DisplayName("제재 대상 선정")
    void applyRestriction_Success() {
        // given
        ReportType reportType = ReportType.builder()
                .reportTypeId(1L)
                .type("욕설 및 비속어 포함")
                .build();

        RestrictionType restrictionType = RestrictionType.builder()
                .restrictionTypeId(1L)
                .content("게시글 작성 제한")
                .duration(7)
                .build();
        // 이 부분을 추가해야 합니다.
        Status pendingStatus = Status.builder()
                .statusId(1L)
                .domain("RESTRICTION")
                .code("PENDING")
                .description("제재 대기중")
                .build();

        when(statusRepository.findByDomainAndCode("RESTRICTION", "PENDING"))
                .thenReturn(Optional.of(pendingStatus));


        when(restrictionTypeRepository.findByContent("게시글 작성 제한")).thenReturn(Optional.of(restrictionType));

        // when
        ReflectionTestUtils.invokeMethod(reportService,"applyRestriction",user1, reportType, "게시글 작성 제한", 7);

        // then
        verify(restrictionTypeRepository).findByContent("게시글 작성 제한");
        verify(restrictionTargetRepository).save(any(RestrictionTarget.class));
    }

    @Test
    @DisplayName("제재 만료 대상 상태변경_빈 리스트")
    void expireRestrictions_emptyList() {
        // when
        reportService.expireRestrictions(new ArrayList<>());

        // then
        verifyNoInteractions(restrictionTargetRepository);
    }

    @Test
    @DisplayName("제재 만료 대상 상태변경_null")
    void expireRestrictions_null() {
        // when
        reportService.expireRestrictions(null);

        // then
        verifyNoInteractions(restrictionTargetRepository);
    }

    @Test
    @DisplayName("제재 만료 대상 상태변경_성공")
    void expireRestrictions_Success() {
        // given
        List<Long> ids = List.of(1L, 2L, 3L);
        Status restrictExpire = Status.builder().code("RESTRICT_EXPIRATION").build();
        when(statusRepository.findByCode("RESTRICT_EXPIRATION")).thenReturn(restrictExpire);
        // when
        reportService.expireRestrictions(ids);

        // then
        verify(restrictionTargetRepository).updateStatusForIds(
                eq(ids),
                eq(restrictExpire)
        );
    }

    @Test
    @DisplayName("제재 만료 대상 조회_성공")
    void getRestrictExpiredList_Success() {
        // given
        RestrictionTarget expired1 = RestrictionTarget.builder()
                .user(user1)
                .reportType(ReportType.builder().reportTypeId(1L).type("욕설 및 비속어 포함").build())
                .restrictionType(RestrictionType.builder().content("게시글 작성 제한(7일)").duration(7).build())
                .status(Status.builder().code("COMPLETED").build())
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        RestrictionTarget expired2 = RestrictionTarget.builder()
                .user(user2)
                .reportType(ReportType.builder().reportTypeId(1L).type("욕설 및 비속어 포함").build())
                .restrictionType(RestrictionType.builder().content("게시글 작성 제한(7일)").duration(7).build())
                .status(Status.builder().code("COMPLETED").build())
                .expiresAt(LocalDateTime.now())
                .build();

        Status completed = Status.builder().code("COMPLETED").build();
        when(statusRepository.findByCode("COMPLETED")).thenReturn(completed);
        when(restrictionTargetRepository.findExpiredRestrictions(any(LocalDateTime.class), eq(completed)))
                .thenReturn(List.of(expired1, expired2));

        // when
        RestrictExpiredResponseDto result = reportService.getRestrictExpiredList();

        // then
        assertNotNull(result);
        assertEquals(2, result.getExpiredRestrictions().size());
        assertEquals(100L, result.getExpiredRestrictions().get(0).getUserId());
        assertEquals(101L, result.getExpiredRestrictions().get(1).getUserId());

        verify(restrictionTargetRepository).findExpiredRestrictions(any(LocalDateTime.class), eq(completed));
        verifyNoMoreInteractions(restrictionTargetRepository);
    }

}
