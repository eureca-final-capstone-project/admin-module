package eureca.capstone.project.admin.dashboard.service.impl;

import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.entity.TelecomCompany;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.dashboard.dto.response.DashboardResponseDto;
import eureca.capstone.project.admin.dashboard.dto.response.HourlyPriceStatDto;
import eureca.capstone.project.admin.dashboard.dto.response.VolumeStatDto;
import eureca.capstone.project.admin.dashboard.service.impl.DashboardServiceImpl;
import eureca.capstone.project.admin.dashboard.service.strategy.DailyVolumeStatStrategy;
import eureca.capstone.project.admin.dashboard.service.strategy.HourlyVolumeStatStrategy;
import eureca.capstone.project.admin.dashboard.service.strategy.VolumeStatStrategy;
import eureca.capstone.project.admin.market_statistic.domain.MarketStatistic;
import eureca.capstone.project.admin.market_statistic.domain.TransactionAmountStatistic;
import eureca.capstone.project.admin.market_statistic.repository.MarketStatisticRepository;
import eureca.capstone.project.admin.market_statistic.repository.TransactionAmountStatisticRepository;
import eureca.capstone.project.admin.report.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.transaction_feed.entity.SalesType;
import eureca.capstone.project.admin.transaction_feed.repository.SalesTypeRepository;
import eureca.capstone.project.admin.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ReportHistoryRepository reportHistoryRepository;
    @Mock private MarketStatisticRepository marketStatisticsRepository;
    @Mock private TransactionAmountStatisticRepository transactionAmountStatisticRepository;
    @Mock private SalesTypeRepository salesTypeRepository;
    @Mock private StatusManager statusManager;

    private DashboardServiceImpl dashboardService;

    // 테스트 실행 전 Mock Service 객체 초기화
    @BeforeEach
    void setUp() {
        // 실제 전략 구현체를 리스트로 만듭니다.
        List<VolumeStatStrategy> strategies = List.of(
                new DailyVolumeStatStrategy(),
                new HourlyVolumeStatStrategy()
        );

        // Mock 객체들과 전략 리스트를 사용하여 dashboardService를 수동으로 생성합니다.
        dashboardService = new DashboardServiceImpl(
                userRepository,
                reportHistoryRepository,
                marketStatisticsRepository,
                transactionAmountStatisticRepository,
                salesTypeRepository,
                statusManager,
                strategies
        );
    }

    @Test
    @DisplayName("대시보드 데이터 조회 성공_일반 판매 (시간별)")
    void getDashboardData_Normal_Success() {
        // Given
        Status activeStatus = new Status(1L, "USER", "ACTIVE", "활성");
        when(statusManager.getStatus("USER", "ACTIVE")).thenReturn(activeStatus);
        when(userRepository.countByStatus(activeStatus)).thenReturn(350L);
        when(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(15L);
        when(reportHistoryRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(8L);
        when(reportHistoryRepository.count()).thenReturn(120L);

        when(salesTypeRepository.findByName("일반 판매"))
                .thenReturn(Optional.of(new SalesType(1L, "일반 판매")));

        when(marketStatisticsRepository.findAllByStaticsTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenAnswer(inv -> {
                    LocalDateTime to = inv.getArgument(1);
                    LocalDateTime last = to.minusHours(1);
                    LocalDateTime prev = last.minusHours(1);
                    return List.of(
                            new MarketStatistic(1L, 150L, 100L, last, new TelecomCompany(1L, "SKT")),
                            new MarketStatistic(2L, 149L, 120L, last, new TelecomCompany(2L, "KT")),
                            new MarketStatistic(3L, 148L,  80L, prev, new TelecomCompany(1L, "SKT")),
                            new MarketStatistic(4L, 147L,  90L, prev, new TelecomCompany(2L, "KT"))
                    );
                });

        when(transactionAmountStatisticRepository.findAllByStaticsTimeRange(anyLong(), eq("HOUR"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenAnswer(inv -> {
                    LocalDateTime to = inv.getArgument(3);
                    LocalDateTime last = to.minusHours(1);
                    LocalDateTime prev = last.minusHours(1);
                    return List.of(
                            TransactionAmountStatistic.builder().statisticsId(1L).transactionAmount(220L).staticsTime(last).build(),
                            TransactionAmountStatistic.builder().statisticsId(2L).transactionAmount(170L).staticsTime(prev).build()
                    );
                });

        // When
        DashboardResponseDto result = dashboardService.getDashboardData("일반 판매");

        // Then
        assertNotNull(result);
        assertEquals(350L, result.getTotalUserCount());
        assertEquals(15L,  result.getTodayUserCount());
        assertEquals(120L, result.getTotalReportCount());
        assertEquals(8L,   result.getTodayReportCount());
        assertEquals(24, result.getPriceStats().size());
        assertEquals("일반 판매", result.getVolumeStats().getSalesType());
        assertEquals("HOUR", result.getVolumeStats().getStatisticType());
        assertEquals(24, result.getVolumeStats().getVolumes().size());

        ArgumentCaptor<LocalDateTime> fromCap = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCap   = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(marketStatisticsRepository).findAllByStaticsTimeRange(fromCap.capture(), toCap.capture());

        Map<LocalDateTime, VolumeStatDto> volMap = result.getVolumeStats().getVolumes().stream()
                .collect(Collectors.toMap(
                        v -> LocalDate.parse(v.getDate()).atTime(v.getHour(), 0),
                        v -> v
                ));
        LocalDateTime last = toCap.getValue().minusHours(1);
        LocalDateTime prev = last.minusHours(1);
        assertTrue(volMap.containsKey(last));
        assertTrue(volMap.containsKey(prev));
        assertEquals(220L, volMap.get(last).getSaleVolume());
        assertEquals(170L, volMap.get(prev).getSaleVolume());

        // ArgumentCaptor를 사용하여 statType이 "HOUR"로 올바르게 전달되었는지 확인
        ArgumentCaptor<String> statTypeCap = ArgumentCaptor.forClass(String.class);
        verify(transactionAmountStatisticRepository).findAllByStaticsTimeRange(
                anyLong(), statTypeCap.capture(), any(LocalDateTime.class), any(LocalDateTime.class));
        assertEquals("HOUR", statTypeCap.getValue());
    }

    @Test
    @DisplayName("대시보드 데이터 조회 성공_입찰 판매 (일별)")
    void getDashboardData_Bid_Success() {
        // Given
        Status activeStatus = new Status(1L, "USER", "ACTIVE", "활성");
        when(statusManager.getStatus("USER", "ACTIVE")).thenReturn(activeStatus);
        when(userRepository.countByStatus(activeStatus)).thenReturn(350L);
        when(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(15L);
        when(reportHistoryRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(8L);
        when(reportHistoryRepository.count()).thenReturn(120L);

        when(salesTypeRepository.findByName("입찰 판매"))
                .thenReturn(Optional.of(new SalesType(2L, "입찰 판매")));

        when(marketStatisticsRepository.findAllByStaticsTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        when(transactionAmountStatisticRepository.findAllByStaticsTimeRange(anyLong(), eq("DAY"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenAnswer(inv -> {
                    LocalDateTime to = inv.getArgument(3); // now.with(LocalTime.MIN)
                    LocalDateTime yesterday = to.toLocalDate().minusDays(1).atStartOfDay();
                    LocalDateTime twoDaysAgo = to.toLocalDate().minusDays(2).atStartOfDay();
                    return List.of(
                            TransactionAmountStatistic.builder().statisticsId(1L).transactionAmount(220L).staticsTime(yesterday).build(),
                            TransactionAmountStatistic.builder().statisticsId(2L).transactionAmount(170L).staticsTime(twoDaysAgo).build()
                    );
                });

        // When
        DashboardResponseDto result = dashboardService.getDashboardData("입찰 판매");

        // Then
        assertNotNull(result);
        assertEquals(24, result.getPriceStats().size());
        assertEquals("입찰 판매", result.getVolumeStats().getSalesType());
        assertEquals("DAY", result.getVolumeStats().getStatisticType());
        assertEquals(7, result.getVolumeStats().getVolumes().size());

        Map<String, Long> volumeMap = result.getVolumeStats().getVolumes().stream()
                .collect(Collectors.toMap(
                        VolumeStatDto::getDate,
                        VolumeStatDto::getSaleVolume
                ));
        long sum = volumeMap.values().stream().mapToLong(Long::longValue).sum();
        assertEquals(390L, sum);

        ArgumentCaptor<String> statTypeCap = ArgumentCaptor.forClass(String.class);
        verify(transactionAmountStatisticRepository).findAllByStaticsTimeRange(
                anyLong(), statTypeCap.capture(), any(LocalDateTime.class), any(LocalDateTime.class));
        assertEquals("DAY", statTypeCap.getValue());
    }
}