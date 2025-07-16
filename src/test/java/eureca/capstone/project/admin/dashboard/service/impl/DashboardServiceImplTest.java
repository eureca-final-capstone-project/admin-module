package eureca.capstone.project.admin.dashboard.service.impl;

import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.entity.TelecomCompany;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.dashboard.dto.response.DashboardResponseDto;
import eureca.capstone.project.admin.market_statistic.domain.MarketStatistic;
import eureca.capstone.project.admin.market_statistic.repository.MarketStatisticRepository;
import eureca.capstone.project.admin.report.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportHistoryRepository reportHistoryRepository;

    @Mock
    private MarketStatisticRepository marketStatisticsRepository;

    @Mock
    private StatusManager statusManager;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("대시보드 데이터 조회 성공")
    void getDashboardData_Success() {
        // given
        Status activeStatus = new Status(1L, "USER", "ACTIVE", "활성");

        LocalDateTime fixedNow = LocalDateTime.of(2025, 7, 16, 22, 0);
        LocalDateTime oneHourAgo = fixedNow.minusHours(1); // 21시

        TelecomCompany skt = new TelecomCompany(1L, "SKT");
        TelecomCompany kt = new TelecomCompany(2L, "KT");

        List<MarketStatistic> mockStats = List.of(
                // 22시 데이터
                new MarketStatistic(1L, 150.5, 100, fixedNow, skt),
                new MarketStatistic(2L, 149.8, 120, fixedNow, kt),
                // 21시 데이터
                new MarketStatistic(3L, 148.2, 80, oneHourAgo, skt),
                new MarketStatistic(4L, 147.5, 90, oneHourAgo, kt)
        );

        // 2. Mock 객체의 행동 정의 (Stubbing)
        when(statusManager.getStatus("USER", "ACTIVE")).thenReturn(activeStatus);
        when(userRepository.countByStatus(activeStatus)).thenReturn(350L);
        when(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(15L);
        when(reportHistoryRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(8L);
        when(reportHistoryRepository.count()).thenReturn(120L);
        when(marketStatisticsRepository.findAllByStaticsTimeAfter(any(LocalDateTime.class))).thenReturn(mockStats);

        // when
        DashboardResponseDto result = dashboardService.getDashboardData();

        // then
        // 1. 기본 카운트 검증
        assertNotNull(result);
        assertEquals(350L, result.getTotalUserCount());
        assertEquals(15L, result.getTodayUserCount());
        assertEquals(120L, result.getTotalReportCount());
        assertEquals(8L, result.getTodayReportCount());

        // 2. 통계 데이터 검증 (개수 및 정렬 확인)
        assertEquals(2, result.getPriceStats().size());
        assertEquals(2, result.getVolumeStats().size());

        // 정렬 확인 (첫 번째 데이터는 21시, 두 번째 데이터는 22시여야 함)
        assertEquals(21, result.getPriceStats().get(0).getHour());
        assertEquals(22, result.getPriceStats().get(1).getHour());

        // 3. 통계 데이터 내용 상세 검증 (22시 데이터)
        var priceStatFor22 = result.getPriceStats().get(1);
        assertEquals(2, priceStatFor22.getPricesByCarrier().size());
        assertEquals("2025-07-16", priceStatFor22.getDate());

        var volumeStatFor22 = result.getVolumeStats().get(1);
        assertEquals(220, volumeStatFor22.getGeneralSaleVolume()); // 100 + 120
        assertEquals(0, volumeStatFor22.getAuctionSaleVolume());

        // 4. Mock 객체 호출 여부 검증
        verify(statusManager).getStatus("USER", "ACTIVE");
        verify(userRepository).countByStatus(activeStatus);
        verify(marketStatisticsRepository).findAllByStaticsTimeAfter(any(LocalDateTime.class));
    }
}