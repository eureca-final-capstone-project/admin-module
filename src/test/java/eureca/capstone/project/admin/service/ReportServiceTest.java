package eureca.capstone.project.admin.service;

import eureca.capstone.project.admin.domain.ReportHistory;
import eureca.capstone.project.admin.domain.ReportType;
import eureca.capstone.project.admin.domain.RestrictionTarget;
import eureca.capstone.project.admin.domain.RestrictionType;
import eureca.capstone.project.admin.domain.status.ReportHistoryStatus;
import eureca.capstone.project.admin.domain.status.RestrictionTargetStatus;
import eureca.capstone.project.admin.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.dto.response.ReportCountDto;
import eureca.capstone.project.admin.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.dto.response.RestrictExpiredResponseDto;
import eureca.capstone.project.admin.dto.response.RestrictionDto;
import eureca.capstone.project.admin.exception.AlreadyProcessedReportException;
import eureca.capstone.project.admin.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.repository.RestrictionTargetRepository;
import eureca.capstone.project.admin.repository.RestrictionTypeRepository;
import eureca.capstone.project.admin.service.impl.ReportServiceImpl;
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
public class ReportServiceTest {

    @Mock
    private ReportHistoryRepository reportHistoryRepository;

    @Mock
    private RestrictionTargetRepository restrictionTargetRepository;

    @Mock
    private RestrictionTypeRepository restrictionTypeRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Pageable pageable;
    private ReportHistory report1;
    private ReportHistory report2;
    private RestrictionTarget restriction1;
    private RestrictionTarget restriction2;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10); // 페이지 번호 0, 사이즈 10

        report1 = ReportHistory.builder()
                .userId(100L)
                .transactionFeedId(1L)
                .reason("욕설 및 비속어 포함")
                .reportType(ReportType.builder().reportTypeId(1L).type("욕설 및 비속어 포함").build())
                .status(ReportHistoryStatus.PENDING) // 상태 Enum 사용
                .isModerated(false)
                .build();

        report2 = ReportHistory.builder()
                .userId(101L)
                .transactionFeedId(1L)
                .reason("주제 관련 없음")
                .reportType(ReportType.builder().reportTypeId(2L).type("주제 관련 없음").build())
                .status(ReportHistoryStatus.AI_ACCEPTED) // 상태 Enum 사용
                .isModerated(true)
                .build();

        restriction1 = RestrictionTarget.builder()
                .userId(103L)
                .reportType(ReportType.builder().reportTypeId(1L).type("욕설 및 비속어 포함").build())
                .restrictionType(RestrictionType.builder().content("게시글 작성 제한(7일)").duration(7).build())
                .status(RestrictionTargetStatus.PENDING)
                .build();

        restriction2 = RestrictionTarget.builder()
                .userId(104L)
                .reportType(ReportType.builder().reportTypeId(2L).type("주제 관련 없음").build())
                .restrictionType(RestrictionType.builder().content("게시글 작성 제한(1일)").duration(1).build())
                .status(RestrictionTargetStatus.ACCEPTED)
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
        Page<ReportHistoryDto> result = reportService.getReportHistoryList(null, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(report1.getReportHistoryId(), result.getContent().get(0).getReportHistoryId());
        assertEquals(report2.getReportHistoryId(), result.getContent().get(1).getReportHistoryId());
        assertEquals(report1.getUserId(), result.getContent().get(0).getUserId());
        verify(reportHistoryRepository).findAll(pageable);
    }


    @Test
    @DisplayName("신고 내역 필터링 조회_성공")
    void getReportHistory_filtering_Success() {
        // given
        ReportHistoryStatus reportStatus = ReportHistoryStatus.PENDING;
        Page<ReportHistory> page = new PageImpl<>(List.of(report1));
        when(reportHistoryRepository.findByStatus(reportStatus, pageable)).thenReturn(page);

        // when
        Page<ReportHistoryDto> result = reportService.getReportHistoryList(reportStatus, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(report1.getReportHistoryId(), result.getContent().get(0).getReportHistoryId());
        assertEquals(report1.getUserId(), result.getContent().get(0).getUserId());
        verify(reportHistoryRepository).findByStatus(reportStatus, pageable);
    }

    @Test
    @DisplayName("제재 내역 전체 조회_성공")
    void getRestrictionList_all_Success() {
        // given
        Page<RestrictionTarget> page = new PageImpl<>(List.of(restriction1, restriction2));
        when(restrictionTargetRepository.findAll(pageable)).thenReturn(page);

        // when
        Page<RestrictionDto> result = reportService.getRestrictionList(null, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(restriction1.getUserId(), result.getContent().get(0).getUserId());
        verify(restrictionTargetRepository).findAll(pageable);
    }

    @Test
    @DisplayName("제재 내역 상태 필터링 조회_성공")
    void getRestrictionList_filtered_Success() {
        // given
        RestrictionTargetStatus targetStatus = RestrictionTargetStatus.PENDING;
        Page<RestrictionTarget> page = new PageImpl<>(List.of(restriction1));
        when(restrictionTargetRepository.findByStatus(targetStatus, pageable)).thenReturn(page);

        // when
        Page<RestrictionDto> result = reportService.getRestrictionList(targetStatus, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(restriction1.getUserId(), result.getContent().get(0).getUserId());
        verify(restrictionTargetRepository).findByStatus(targetStatus, pageable);
    }

    @Test
    @DisplayName("관리자 신고_미승인")
    void processReportByAdmin_reject_Success() {
        // given
        ProcessReportDto request = new ProcessReportDto();
        ReflectionTestUtils.setField(request, "approved", false);
        when(reportHistoryRepository.findById(1L)).thenReturn(Optional.of(report1));

        // when
        reportService.processReportByAdmin(1L, request);

        // then
        assertEquals(ReportHistoryStatus.ADMIN_REJECTED, report1.getStatus());
        verify(reportHistoryRepository).findById(1L);
    }

    @Test
    @DisplayName("관리자 신고_승인")
    void processReportByAdmin_approve_Success() {
        // given
        ProcessReportDto request = new ProcessReportDto();
        ReflectionTestUtils.setField(request, "approved", true);
        when(reportHistoryRepository.findById(1L)).thenReturn(Optional.of(report1));

        // when
        reportService.processReportByAdmin(1L, request);

        // then
        assertEquals(ReportHistoryStatus.ADMIN_ACCEPTED, report1.getStatus());
        verify(reportHistoryRepository).findById(1L);
    }

    @Test
    @DisplayName("관리자 신고_이미 처리됨 예외")
    void processReportByAdmin_alreadyProcessed_exception() {
        // given
        ProcessReportDto request = new ProcessReportDto();
        ReflectionTestUtils.setField(request, "approved", true);

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
                .type("욕설 및 비속어 포함 ")
                .build();

        RestrictionType restrictionType = RestrictionType.builder()
                .restrictionTypeId(1L)
                .content("게시글 작성 제한(7일)")
                .duration(7)
                .build();

        when(restrictionTypeRepository.findByContent("게시글 작성 제한(7일)")).thenReturn(Optional.of(restrictionType));

        // when
        ReflectionTestUtils.invokeMethod(reportService,"applyRestriction",100L, reportType, "게시글 작성 제한(7일)", 7);

        // then
        verify(restrictionTypeRepository).findByContent("게시글 작성 제한(7일)");
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

        // when
        reportService.expireRestrictions(ids);

        // then
        verify(restrictionTargetRepository).updateStatusForIds(
                eq(ids),
                eq(RestrictionTargetStatus.EXPIRED)
        );
    }

    @Test
    @DisplayName("제재 만료 대상 조회_성공")
    void getRestrictExpiredList_Success() {
        // given
        RestrictionTarget expired1 = RestrictionTarget.builder()
                .userId(100L)
                .reportType(ReportType.builder().reportTypeId(1L).type("욕설 및 비속어 포함").build())
                .restrictionType(RestrictionType.builder().content("게시글 작성 제한(7일)").duration(7).build())
                .status(RestrictionTargetStatus.ACCEPTED)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        RestrictionTarget expired2 = RestrictionTarget.builder()
                .userId(101L)
                .reportType(ReportType.builder().reportTypeId(1L).type("욕설 및 비속어 포함").build())
                .restrictionType(RestrictionType.builder().content("게시글 작성 제한(7일)").duration(7).build())
                .status(RestrictionTargetStatus.ACCEPTED)
                .expiresAt(LocalDateTime.now())
                .build();

        when(restrictionTargetRepository.findExpiredRestrictions(any(LocalDateTime.class), eq(RestrictionTargetStatus.ACCEPTED)))
                .thenReturn(List.of(expired1, expired2));

        // when
        RestrictExpiredResponseDto result = reportService.getRestrictExpiredList();

        // then
        assertNotNull(result);
        assertEquals(2, result.getExpiredRestrictions().size());
        assertEquals(100L, result.getExpiredRestrictions().get(0).getUserId());
        assertEquals(101L, result.getExpiredRestrictions().get(1).getUserId());

        verify(restrictionTargetRepository).findExpiredRestrictions(any(LocalDateTime.class), eq(RestrictionTargetStatus.ACCEPTED));
        verifyNoMoreInteractions(restrictionTargetRepository);
    }

}
