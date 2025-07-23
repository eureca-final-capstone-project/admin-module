package eureca.capstone.project.admin.report.service;

import eureca.capstone.project.admin.auth.entity.Authority;
import eureca.capstone.project.admin.auth.entity.UserAuthority;
import eureca.capstone.project.admin.auth.repository.UserAuthorityRepository;
import eureca.capstone.project.admin.common.exception.custom.AlreadyProcessedRestrictionException;
import eureca.capstone.project.admin.common.exception.custom.ReportNotFoundException;
import eureca.capstone.project.admin.common.exception.custom.RestrictionTargetNotFoundException;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.report.dto.response.*;
import eureca.capstone.project.admin.report.entity.ReportHistory;
import eureca.capstone.project.admin.report.entity.ReportType;
import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import eureca.capstone.project.admin.report.entity.RestrictionType;
import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.admin.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.admin.user.entity.User;
import eureca.capstone.project.admin.report.dto.request.ProcessReportDto;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportHistoryRepository reportHistoryRepository;

    @Mock
    private RestrictionTargetRepository restrictionTargetRepository;

    @Mock
    private UserAuthorityRepository userAuthorityRepository;

    @Mock
    private RestrictionTypeRepository restrictionTypeRepository;

    @Mock
    private TransactionFeedRepository transactionFeedRepository;

    @Mock
    private StatusManager statusManager;

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
    @DisplayName("신고 내역 목록 조회_조건 없음_성공")
    void getReportHistory_NoCriteria_Success() {
        // given
        String statusCode = null;
        String keyword = null;
        Page<ReportHistory> page = new PageImpl<>(List.of(report1, report2));
        when(reportHistoryRepository.findByCriteria(statusCode, keyword, pageable)).thenReturn(page);

        // when
        Page<ReportHistoryDto> result = reportService.getReportHistoryListByStatusCode(null,null, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(report1.getReportHistoryId(), result.getContent().get(0).getReportHistoryId());
        assertEquals(report2.getReportHistoryId(), result.getContent().get(1).getReportHistoryId());
        assertEquals(report1.getUser().getUserId(), result.getContent().get(0).getReporterId());
        verify(reportHistoryRepository).findByCriteria(statusCode, keyword, pageable);
    }


    @Test
    @DisplayName("신고 내역 목록 조회_상태코드로 필터링_성공")
    void getReportHistory_filtering_Success() {
        // given
        String statusCode = "PENDING";
        String keyword = null;
        Page<ReportHistory> page = new PageImpl<>(List.of(report1));

        when(reportHistoryRepository.findByCriteria(statusCode,keyword, pageable)).thenReturn(page);

        // when
        Page<ReportHistoryDto> result = reportService.getReportHistoryListByStatusCode(statusCode,keyword, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("검수 대기중", result.getContent().get(0).getStatus());
        verify(reportHistoryRepository).findByCriteria(statusCode, keyword, pageable);
    }

    @Test
    @DisplayName("신고 내역 목록 조회_키워드로 검색_성공")
    void getReportHistoryList_SearchByKeyword_Success(){
        // given
        String keyword = "reporter";
        String statusCode = null;
        Page<ReportHistory> page = new PageImpl<>(List.of(report1));
        when(reportHistoryRepository.findByCriteria(statusCode, keyword, pageable)).thenReturn(page);

        // when
        Page<ReportHistoryDto> result = reportService.getReportHistoryListByStatusCode(statusCode, keyword, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(user1.getEmail(), result.getContent().get(0).getReporterEmail());
        verify(reportHistoryRepository).findByCriteria(statusCode, keyword, pageable);
    }




    @Test
    @DisplayName("관리자 신고_미승인")
    void processReportByAdmin_reject_Success() {
        // given
        ProcessReportDto request = new ProcessReportDto();
        ReflectionTestUtils.setField(request, "approved", false);
        Status pending = Status.builder().statusId(25L).code("PENDING").build();
        Status aiRejected = Status.builder().statusId(27L).code("AI_REJECTED").build();
        Status adminRejected = Status.builder().statusId(29L).code("ADMIN_REJECTED").build();

        report1.updateStatus(pending);

        when(statusManager.getStatus("REPORT", "PENDING")).thenReturn(pending);
        when(statusManager.getStatus("REPORT", "AI_REJECTED")).thenReturn(aiRejected);
        when(statusManager.getStatus("REPORT", "ADMIN_REJECTED")).thenReturn(adminRejected);
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

        Status pending = Status.builder().statusId(25L).code("PENDING").build();
        Status aiRejected = Status.builder().statusId(27L).code("AI_REJECTED").build();
        Status adminAccepted = Status.builder().statusId(28L).code("ADMIN_ACCEPTED").build();
        Status aiAccepted = Status.builder().statusId(26L).code("AI_ACCEPTED").build();

        report1.updateStatus(pending);

        when(reportHistoryRepository.findById(1L)).thenReturn(Optional.of(report1));

        when(statusManager.getStatus("REPORT", "PENDING")).thenReturn(pending);
        when(statusManager.getStatus("REPORT", "AI_REJECTED")).thenReturn(aiRejected);
        when(statusManager.getStatus("REPORT", "ADMIN_ACCEPTED")).thenReturn(adminAccepted);
        when(statusManager.getStatus("REPORT", "AI_ACCEPTED")).thenReturn(aiAccepted);

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

        // 변경점: findByCode -> findByDomainAndCode
        when(statusManager.getStatus("REPORT","PENDING")).thenReturn(Status.builder().statusId(25L).code("PENDING").build());
        when(statusManager.getStatus("REPORT","AI_REJECTED")).thenReturn(Status.builder().statusId(27L).code("AI_REJECTED").build());
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

        Status pendingStatus = Status.builder()
                .statusId(1L)
                .domain("RESTRICTION")
                .code("PENDING")
                .description("제재 대기중")
                .build();

        Status aiAccept = Status.builder()
                .statusId(2L)
                .domain("REPORT")
                .code("AI_ACCEPTED")
                .description("AI 승인")
                .build();

        Status adminAccept = Status.builder()
                .statusId(3L)
                .domain("REPORT")
                .code("ADMIN_ACCEPTED")
                .description("관리자 승인")
                .build();
        List<Status> acceptedStatuses = List.of(aiAccept, adminAccept);

        when(restrictionTypeRepository.findById(1L)).thenReturn(Optional.of(restrictionType));
        when(statusManager.getStatus("RESTRICTION", "PENDING"))
                .thenReturn(pendingStatus);

        // when
        ReflectionTestUtils.invokeMethod(reportService,"applyRestriction", user1, reportType, 1L, acceptedStatuses);

        // then
        verify(restrictionTargetRepository).save(any(RestrictionTarget.class));
        verify(reportHistoryRepository).saveAll(anyList());
    }


    @Test
    @DisplayName("신고 상세 조회 성공")
    void getReportDetail_Success() {
        // given
        Long reportId = 1L;
        ReportDetailResponseDto mockDto = ReportDetailResponseDto.builder()
                .reportId(reportId)
                .status("AI 거절")
                .reporterEmail("reporter@example.com")
                .build();

        when(reportHistoryRepository.getReportDetail(reportId)).thenReturn(mockDto);

        // when
        ReportDetailResponseDto result = reportService.getReportDetail(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getReporterEmail()).isEqualTo("reporter@example.com");
    }


    @Test
    @DisplayName("신고 상세 조회_실패(ReportNotFound)")
    void getReportDetail_Fail_ReportNotFound() {
        // given
        Long reportId = 9999L;
        when(reportHistoryRepository.getReportDetail(reportId)).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> reportService.getReportDetail(reportId))
                .isInstanceOf(ReportNotFoundException.class);
    }

}
