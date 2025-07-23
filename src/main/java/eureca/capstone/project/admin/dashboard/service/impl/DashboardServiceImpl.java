package eureca.capstone.project.admin.dashboard.service.impl;


import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.dashboard.dto.response.CarrierPriceDto;
import eureca.capstone.project.admin.dashboard.dto.response.DashboardResponseDto;
import eureca.capstone.project.admin.dashboard.dto.response.HourlyPriceStatDto;
import eureca.capstone.project.admin.dashboard.dto.response.HourlyVolumeStatDto;
import eureca.capstone.project.admin.dashboard.service.DashboardService;
import eureca.capstone.project.admin.market_statistic.domain.MarketStatistic;
import eureca.capstone.project.admin.market_statistic.domain.TransactionAmountStatistic;
import eureca.capstone.project.admin.market_statistic.repository.MarketStatisticRepository;
import eureca.capstone.project.admin.market_statistic.repository.TransactionAmountStatisticRepository;
import eureca.capstone.project.admin.report.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ReportHistoryRepository reportHistoryRepository;
    private final MarketStatisticRepository marketStatisticsRepository;
    private final TransactionAmountStatisticRepository transactionAmountStatisticRepository;
    private final StatusManager statusManager;

    @Override
    public DashboardResponseDto getDashboardData() {
        Status activeStatus = statusManager.getStatus("USER", "ACTIVE");
        long totalUserCount = userRepository.countByStatus(activeStatus);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long todayUserCount = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        long todayReportCount = reportHistoryRepository.countByCreatedAtAfter(startOfDay);
        long totalReportCount = reportHistoryRepository.count();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime startTime = endTime.minusHours(24);

        List<MarketStatistic> recentStats = marketStatisticsRepository.findAllByStaticsTimeRange(startTime, endTime);
        List<TransactionAmountStatistic> recentVolumeStats = transactionAmountStatisticRepository.findAllByStaticsTimeRange(startTime, endTime);

        // 3. 조회된 통계를 '시간(statisticsTime)'을 기준으로 그룹화
        Map<LocalDateTime, List<MarketStatistic>> marketStatsGroupedByTime = recentStats.stream()
                .collect(Collectors.groupingBy(MarketStatistic::getStaticsTime));

        Map<LocalDateTime, Long> volumeStatsGroupedByTime =
                recentVolumeStats.stream()
                        .collect(Collectors.groupingBy(
                                TransactionAmountStatistic::getStaticsTime,
                                Collectors.summingLong(TransactionAmountStatistic::getTransactionAmount)
                        ));

        List<LocalDateTime> times = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            times.add(startTime.plusHours(i));
        }

        // 4. DTO로 변환할 리스트 초기화
        List<HourlyPriceStatDto> priceStatsDtoList = new ArrayList<>();
        List<HourlyVolumeStatDto> volumeStatsDtoList = new ArrayList<>();

        // 5. 그룹화된 데이터를 순회하며 DTO 생성
        for (LocalDateTime time : times) {

            // 시세 통계 DTO 생성
            List<CarrierPriceDto> pricesByCarrier =
                    marketStatsGroupedByTime.getOrDefault(time, Collections.emptyList())
                            .stream()
                            .map(stat -> CarrierPriceDto.builder()
                                    .carrierName(stat.getTelecomCompany().getName())
                                    .pricePerGb(stat.getAveragePrice())
                                    .build())
                            .toList();

            priceStatsDtoList.add(HourlyPriceStatDto.builder()
                    .date(time.toLocalDate().toString())
                    .hour(time.getHour())
                    .pricesByCarrier(pricesByCarrier)
                    .build());

            // 거래량 통계 DTO 생성
            long saleVolume = volumeStatsGroupedByTime.getOrDefault(time, 0L);

            volumeStatsDtoList.add(HourlyVolumeStatDto.builder()
                    .date(time.toLocalDate().toString())
                    .hour(time.getHour())
                    .saleVolume(saleVolume)
                    .build());
        }

        // 6. 최종 DTO로 조합하여 반환
        return DashboardResponseDto.builder()
                .todayUserCount(todayUserCount)
                .totalUserCount(totalUserCount)
                .todayReportCount(todayReportCount)
                .totalReportCount(totalReportCount)
                .priceStats(priceStatsDtoList)
                .volumeStats(volumeStatsDtoList)
                .build();
    }
}
