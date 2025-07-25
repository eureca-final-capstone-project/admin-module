package eureca.capstone.project.admin.dashboard.service.impl;


import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.exception.custom.SalesTypeNotFoundException;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.dashboard.dto.response.*;
import eureca.capstone.project.admin.dashboard.service.DashboardService;
import eureca.capstone.project.admin.market_statistic.domain.MarketStatistic;
import eureca.capstone.project.admin.market_statistic.domain.TransactionAmountStatistic;
import eureca.capstone.project.admin.market_statistic.repository.MarketStatisticRepository;
import eureca.capstone.project.admin.market_statistic.repository.TransactionAmountStatisticRepository;
import eureca.capstone.project.admin.report.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.transaction_feed.entity.SalesType;
import eureca.capstone.project.admin.transaction_feed.repository.SalesTypeRepository;
import eureca.capstone.project.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ReportHistoryRepository reportHistoryRepository;
    private final MarketStatisticRepository marketStatisticsRepository;
    private final TransactionAmountStatisticRepository transactionAmountStatisticRepository;
    private final SalesTypeRepository salesTypeRepository;
    private final StatusManager statusManager;

    @Override
    public DashboardResponseDto getDashboardData(String salesTypeName) {
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

        SalesType salesType = salesTypeRepository.findByName(salesTypeName)
                .orElseThrow(SalesTypeNotFoundException::new);
        Long salesTypeId = salesType.getSalesTypeId();
        String statType = salesTypeName.equals("일반 판매") ? "HOUR" : "DAY";
        log.info("[getDashboardData] salesType: {}, statType: {}", salesTypeName, statType);

        // 시세 통계
        List<MarketStatistic> recentStats = marketStatisticsRepository.findAllByStaticsTimeRange(startTime, endTime);
        List<HourlyPriceStatDto> priceStatsDtoList = buildPriceStats(startTime, endTime, recentStats);
        log.info("[getDashboardData] {} {}시 ~ {} {}시 시세통계 조회 {}건", startTime.toLocalDate(), startTime.getHour(), endTime.toLocalDate(), endTime.getHour()-1, priceStatsDtoList.size());

        // 거래량 통계
        if("DAY".equals(statType)) {
            startTime = endTime.minusDays(7).with(LocalTime.MIN);
            endTime   = endTime.with(LocalTime.MIN);
        }

        List<TransactionAmountStatistic> volumeStats =
                transactionAmountStatisticRepository.findAllByStaticsTimeRange(salesTypeId, statType, startTime, endTime);
        log.info("[getDashboardData] 거래량 통계 조회 {}건. (시간: {} ~ {})", volumeStats.size(), startTime, endTime);

        List<VolumeStatDto> volumeStatsDtoList =
                "HOUR".equals(statType)
                        ? buildHourlyVolumeDtos(startTime, endTime, volumeStats)
                        : buildDailyVolumeDtos(startTime.toLocalDate(), endTime.toLocalDate().minusDays(1), volumeStats);

        TransactionVolumeStatDto volumeResponse = TransactionVolumeStatDto.builder()
                .salesType(salesType.getName())
                .statisticType(statType)
                .volumes(volumeStatsDtoList)
                .build();

        return DashboardResponseDto.builder()
                .todayUserCount(todayUserCount)
                .totalUserCount(totalUserCount)
                .todayReportCount(todayReportCount)
                .totalReportCount(totalReportCount)
                .priceStats(priceStatsDtoList)
                .volumeStats(volumeResponse)
                .build();
    }

    @Override
    public TransactionVolumeStatDto transactionVolumeStatData(String salesTypeName) {

        SalesType salesType = salesTypeRepository.findByName(salesTypeName)
                .orElseThrow(SalesTypeNotFoundException::new);
        String statType = salesTypeName.equals("일반 판매") ? "HOUR" : "DAY";
        log.info("[transactionVolumeStatData] salesType={}, statType={}", salesTypeName, statType);

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime startTime = now.minusHours(24);
        LocalDateTime endTime = now;
        if ("DAY".equals(statType)) {
            startTime = now.minusDays(7).with(LocalTime.MIN);
            endTime = now.with(LocalTime.MIN);
        }

        List<TransactionAmountStatistic> volumeStats =
                transactionAmountStatisticRepository.findAllByStaticsTimeRange(
                        salesType.getSalesTypeId(),
                        statType,
                        startTime,
                        endTime
                );
        log.info("[transactionVolumeStatData] 거래량 통계 조회 {}건. (시간: {} ~ {})", volumeStats.size(), startTime, endTime);

        List<VolumeStatDto> volumes = "HOUR".equals(statType)
                ? buildHourlyVolumeDtos(startTime, endTime, volumeStats)
                : buildDailyVolumeDtos(startTime.toLocalDate(), endTime.toLocalDate().minusDays(1), volumeStats);

        return TransactionVolumeStatDto.builder()
                .salesType(salesTypeName)
                .statisticType(statType)
                .volumes(volumes)
                .build();
    }

    private List<HourlyPriceStatDto> buildPriceStats(LocalDateTime start, LocalDateTime end,
                                                     List<MarketStatistic> recentStats) {

        Map<LocalDateTime, List<MarketStatistic>> group = recentStats.stream()
                .collect(Collectors.groupingBy(MarketStatistic::getStaticsTime));

        List<HourlyPriceStatDto> list = new ArrayList<>();
        for (LocalDateTime t = start; !t.isAfter(end.minusHours(1)); t = t.plusHours(1)) {
            List<CarrierPriceDto> pricesByCarrier =
                    group.getOrDefault(t, Collections.emptyList())
                            .stream()
                            .map(stat -> CarrierPriceDto.builder()
                                    .carrierName(stat.getTelecomCompany().getName())
                                    .pricePerMb(stat.getAveragePrice())
                                    .build())
                            .toList();

            list.add(HourlyPriceStatDto.builder()
                    .date(t.toLocalDate().toString())
                    .hour(t.getHour())
                    .pricesByCarrier(pricesByCarrier)
                    .build());
        }
        return list;
    }

    private List<VolumeStatDto> buildHourlyVolumeDtos(LocalDateTime start, LocalDateTime end,
                                                      List<TransactionAmountStatistic> stats) {
        Map<LocalDateTime, Long> map = stats.stream()
                .collect(Collectors.toMap(TransactionAmountStatistic::getStaticsTime,
                        TransactionAmountStatistic::getTransactionAmount));
        List<VolumeStatDto> result = new ArrayList<>();
        for (LocalDateTime t = start; !t.isAfter(end.minusHours(1)); t = t.plusHours(1)) {
            result.add(VolumeStatDto.builder()
                    .date(t.toLocalDate().toString())
                    .hour(t.getHour())
                    .saleVolume(map.getOrDefault(t, 0L))
                    .build());
        }
        return result;
    }

    private List<VolumeStatDto> buildDailyVolumeDtos(LocalDate start, LocalDate end,
                                                     List<TransactionAmountStatistic> stats) {
        Map<LocalDate, Long> map = stats.stream()
                .collect(Collectors.toMap(s -> s.getStaticsTime().toLocalDate(),
                        TransactionAmountStatistic::getTransactionAmount));
        List<VolumeStatDto> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            result.add(VolumeStatDto.builder()
                    .date(d.toString())
                    .hour(null)
                    .saleVolume(map.getOrDefault(d, 0L))
                    .build());
        }
        return result;
    }

}
