package eureca.capstone.project.admin.dashboard.service.impl;

import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.entity.TelecomCompany;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.dashboard.dto.response.DashboardResponseDto;
import eureca.capstone.project.admin.dashboard.dto.response.HourlyPriceStatDto;
import eureca.capstone.project.admin.dashboard.dto.response.VolumeStatDto;
import eureca.capstone.project.admin.market_statistic.domain.MarketStatistic;
import eureca.capstone.project.admin.market_statistic.domain.TransactionAmountStatistic;
import eureca.capstone.project.admin.market_statistic.repository.MarketStatisticRepository;
import eureca.capstone.project.admin.market_statistic.repository.TransactionAmountStatisticRepository;
import eureca.capstone.project.admin.report.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.transaction_feed.entity.SalesType;
import eureca.capstone.project.admin.transaction_feed.repository.SalesTypeRepository;
import eureca.capstone.project.admin.user.repository.UserRepository;
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

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("대시보드 데이터 조회 성공_일반 판매 (시간별)")
    void getDashboardData_Normal_Success() {
        Status activeStatus = new Status(1L, "USER", "ACTIVE", "활성");
        when(statusManager.getStatus("USER", "ACTIVE")).thenReturn(activeStatus);
        when(userRepository.countByStatus(activeStatus)).thenReturn(350L);
        when(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(15L);
        when(reportHistoryRepository.countByCreatedAtAfter(any())).thenReturn(8L);
        when(reportHistoryRepository.count()).thenReturn(120L);

        when(salesTypeRepository.findByName("일반 판매"))
                .thenReturn(Optional.of(new SalesType(1L, "일반 판매")));

        when(marketStatisticsRepository.findAllByStaticsTimeRange(any(), any()))
                .thenAnswer(inv -> {
                    LocalDateTime from = inv.getArgument(0);
                    LocalDateTime to   = inv.getArgument(1);
                    LocalDateTime last = to.minusHours(1);
                    LocalDateTime prev = last.minusHours(1);
                    return List.of(
                            new MarketStatistic(1L, 150L, 100L, last, new TelecomCompany(1L, "SKT")),
                            new MarketStatistic(2L, 149L, 120L, last, new TelecomCompany(2L, "KT")),
                            new MarketStatistic(3L, 148L,  80L, prev, new TelecomCompany(1L, "SKT")),
                            new MarketStatistic(4L, 147L,  90L, prev, new TelecomCompany(2L, "KT"))
                    );
                });

        when(transactionAmountStatisticRepository.findAllByStaticsTimeRange(anyLong(), anyString(), any(), any()))
                .thenAnswer(inv -> {
                    LocalDateTime to   = inv.getArgument(3);
                    LocalDateTime last = to.minusHours(1);
                    LocalDateTime prev = last.minusHours(1);
                    return List.of(
                            TransactionAmountStatistic.builder().statisticsId(1L).transactionAmount(220L).staticsTime(last).build(),
                            TransactionAmountStatistic.builder().statisticsId(2L).transactionAmount(170L).staticsTime(prev).build()
                    );
                });

        DashboardResponseDto result = dashboardService.getDashboardData("일반 판매");

        assertNotNull(result);
        assertEquals(350L, result.getTotalUserCount());
        assertEquals(15L,  result.getTodayUserCount());
        assertEquals(120L, result.getTotalReportCount());
        assertEquals(8L,   result.getTodayReportCount());
        assertEquals(24, result.getPriceStats().size());
        assertEquals(24, result.getVolumeStats().getVolumes().size());

        ArgumentCaptor<LocalDateTime> fromCap = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCap   = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(marketStatisticsRepository).findAllByStaticsTimeRange(fromCap.capture(), toCap.capture());
        LocalDateTime start = fromCap.getValue();

        List<Integer> expectedHours = IntStream.range(0, 24)
                .map(i -> start.plusHours(i).getHour())
                .boxed().toList();
        List<Integer> actualPriceHours = result.getPriceStats().stream()
                .map(HourlyPriceStatDto::getHour).toList();
        assertEquals(expectedHours, actualPriceHours);

        ArgumentCaptor<String> statTypeCap = ArgumentCaptor.forClass(String.class);
        verify(transactionAmountStatisticRepository).findAllByStaticsTimeRange(
                anyLong(), statTypeCap.capture(), any(), any());
        assertEquals("HOUR", statTypeCap.getValue());

        Map<LocalDateTime, VolumeStatDto> volMap = result.getVolumeStats().getVolumes().stream()
                .collect(Collectors.toMap(
                        v -> LocalDate.parse(v.getDate()).atTime(LocalTime.of(v.getHour(), 0)),
                        v -> v
                ));
        LocalDateTime last = toCap.getValue().minusHours(1);
        LocalDateTime prev = last.minusHours(1);
        assertTrue(volMap.containsKey(last));
        assertTrue(volMap.containsKey(prev));
        assertEquals(24, result.getPriceStats().size());
        assertEquals(24, result.getVolumeStats().getVolumes().size());
        assertEquals(220L, volMap.get(last).getSaleVolume());
        assertEquals(170L, volMap.get(prev).getSaleVolume());
    }

    @Test
    @DisplayName("대시보드 데이터 조회 성공_입찰 판매 (일별)")
    void getDashboardData_Bid_Success() {
        Status activeStatus = new Status(1L, "USER", "ACTIVE", "활성");
        when(statusManager.getStatus("USER", "ACTIVE")).thenReturn(activeStatus);
        when(userRepository.countByStatus(activeStatus)).thenReturn(350L);
        when(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(15L);
        when(reportHistoryRepository.countByCreatedAtAfter(any())).thenReturn(8L);
        when(reportHistoryRepository.count()).thenReturn(120L);

        when(salesTypeRepository.findByName("입찰 판매"))
                .thenReturn(Optional.of(new SalesType(2L, "입찰 판매")));

        when(marketStatisticsRepository.findAllByStaticsTimeRange(any(), any()))
                .thenReturn(Collections.emptyList());

        when(transactionAmountStatisticRepository.findAllByStaticsTimeRange(anyLong(), anyString(), any(), any()))
                .thenAnswer(inv -> {
                    LocalDateTime to = inv.getArgument(3);
                    LocalDateTime lastDay = to.minusDays(1);
                    LocalDateTime prevDay = lastDay.minusDays(1);
                    return List.of(
                            TransactionAmountStatistic.builder().statisticsId(1L).transactionAmount(220L).staticsTime(lastDay).build(),
                            TransactionAmountStatistic.builder().statisticsId(2L).transactionAmount(170L).staticsTime(prevDay).build()
                    );
                });

        DashboardResponseDto result = dashboardService.getDashboardData("입찰 판매");

        assertNotNull(result);
        assertEquals(24, result.getPriceStats().size());
        assertEquals(7, result.getVolumeStats().getVolumes().size());

        ArgumentCaptor<String> statTypeCap = ArgumentCaptor.forClass(String.class);
        verify(transactionAmountStatisticRepository).findAllByStaticsTimeRange(
                anyLong(), statTypeCap.capture(), any(), any());
        assertEquals("DAY", statTypeCap.getValue());

        Map<String, Long> volumeMap = result.getVolumeStats().getVolumes().stream()
                .collect(Collectors.toMap(
                        VolumeStatDto::getDate,
                        VolumeStatDto::getSaleVolume
                ));
        long sum = volumeMap.values().stream().mapToLong(Long::longValue).sum();
        assertEquals(390L, sum);
    }

}
