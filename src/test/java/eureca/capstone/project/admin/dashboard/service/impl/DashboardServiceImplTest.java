package eureca.capstone.project.admin.dashboard.service.impl;

import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.entity.TelecomCompany;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.dashboard.dto.response.DashboardResponseDto;
import eureca.capstone.project.admin.dashboard.dto.response.HourlyPriceStatDto;
import eureca.capstone.project.admin.dashboard.dto.response.HourlyVolumeStatDto;
import eureca.capstone.project.admin.market_statistic.domain.MarketStatistic;
import eureca.capstone.project.admin.market_statistic.domain.TransactionAmountStatistic;
import eureca.capstone.project.admin.market_statistic.repository.MarketStatisticRepository;
import eureca.capstone.project.admin.market_statistic.repository.TransactionAmountStatisticRepository;
import eureca.capstone.project.admin.report.repository.ReportHistoryRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ReportHistoryRepository reportHistoryRepository;
    @Mock private MarketStatisticRepository marketStatisticsRepository;
    @Mock private TransactionAmountStatisticRepository transactionAmountStatisticRepository;
    @Mock private StatusManager statusManager;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("대시보드 데이터 조회 성공")
    void getDashboardData_Success() {
        // 공통 stub
        Status activeStatus = new Status(1L, "USER", "ACTIVE", "활성");
        when(statusManager.getStatus("USER", "ACTIVE")).thenReturn(activeStatus);
        when(userRepository.countByStatus(activeStatus)).thenReturn(350L);
        when(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(15L);
        when(reportHistoryRepository.countByCreatedAtAfter(any())).thenReturn(8L);
        when(reportHistoryRepository.count()).thenReturn(120L);

        // 통신사 더미
        TelecomCompany skt = new TelecomCompany(1L, "SKT");
        TelecomCompany kt  = new TelecomCompany(2L, "KT");

        // from/to를 받아 그 안에 맞는 2개 시간대 데이터(마지막 2시간) 생성
        when(marketStatisticsRepository.findAllByStaticsTimeRange(any(), any()))
                .thenAnswer(inv -> {
                    LocalDateTime from = inv.getArgument(0);
                    LocalDateTime to   = inv.getArgument(1);
                    LocalDateTime last = to.minusHours(1);      // 포함되는 마지막 정각
                    LocalDateTime prev = last.minusHours(1);

                    return List.of(
                            new MarketStatistic(1L, 150L, 100L, last, skt),
                            new MarketStatistic(2L, 149L, 120L, last, kt),
                            new MarketStatistic(3L, 148L,  80L, prev, skt),
                            new MarketStatistic(4L, 147L,  90L, prev, kt)
                    );
                });

        when(transactionAmountStatisticRepository.findAllByStaticsTimeRange(any(), any()))
                .thenAnswer(inv -> {
                    LocalDateTime to   = inv.getArgument(1);
                    LocalDateTime last = to.minusHours(1);
                    LocalDateTime prev = last.minusHours(1);
                    return List.of(
                            new TransactionAmountStatistic(1L, 220L, last),
                            new TransactionAmountStatistic(2L, 170L, prev)
                    );
                });

        // when
        DashboardResponseDto result = dashboardService.getDashboardData();

        // then 기본 카운트
        assertNotNull(result);
        assertEquals(350L, result.getTotalUserCount());
        assertEquals(15L,  result.getTodayUserCount());
        assertEquals(120L, result.getTotalReportCount());
        assertEquals(8L,   result.getTodayReportCount());

        // 24개 슬롯
        assertEquals(24, result.getPriceStats().size());
        assertEquals(24, result.getVolumeStats().size());

        // 서비스에서 전달한 from/to 캡처 -> 기대 hour 시퀀스 구성
        ArgumentCaptor<LocalDateTime> fromCap = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCap   = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(marketStatisticsRepository).findAllByStaticsTimeRange(fromCap.capture(), toCap.capture());
        LocalDateTime start = fromCap.getValue();

        List<Integer> expectedHours = IntStream.range(0, 24)
                .map(i -> start.plusHours(i).getHour())
                .boxed()
                .toList();

        List<Integer> actualPriceHours = result.getPriceStats().stream()
                .map(HourlyPriceStatDto::getHour)
                .toList();
        assertEquals(expectedHours, actualPriceHours, "Price 시간 슬롯 순서 불일치");

        List<Integer> actualVolumeHours = result.getVolumeStats().stream()
                .map(HourlyVolumeStatDto::getHour)
                .toList();
        assertEquals(expectedHours, actualVolumeHours, "Volume 시간 슬롯 순서 불일치");

        // DTO -> LocalDateTime 매핑
        Map<LocalDateTime, HourlyPriceStatDto> priceDtoMap = result.getPriceStats().stream()
                .collect(Collectors.toMap(
                        p -> LocalDate.parse(p.getDate()).atTime(LocalTime.of(p.getHour(), 0)),
                        p -> p
                ));
        Map<LocalDateTime, HourlyVolumeStatDto> volumeDtoMap = result.getVolumeStats().stream()
                .collect(Collectors.toMap(
                        v -> LocalDate.parse(v.getDate()).atTime(LocalTime.of(v.getHour(), 0)),
                        v -> v
                ));

        // 우리가 삽입한 두 시간대(last, prev) 값 검증
        LocalDateTime last = toCap.getValue().minusHours(1);
        LocalDateTime prev = last.minusHours(1);

        assertTrue(priceDtoMap.containsKey(last), "Price DTO에 마지막 시간대가 없음");
        assertTrue(priceDtoMap.containsKey(prev), "Price DTO에 이전 시간대가 없음");
        assertTrue(volumeDtoMap.containsKey(last), "Volume DTO에 마지막 시간대가 없음");
        assertTrue(volumeDtoMap.containsKey(prev), "Volume DTO에 이전 시간대가 없음");

        assertEquals(2, priceDtoMap.get(last).getPricesByCarrier().size());
        assertEquals(2, priceDtoMap.get(prev).getPricesByCarrier().size());

        assertEquals(220L, volumeDtoMap.get(last).getSaleVolume());
        assertEquals(170L, volumeDtoMap.get(prev).getSaleVolume());
    }
}
