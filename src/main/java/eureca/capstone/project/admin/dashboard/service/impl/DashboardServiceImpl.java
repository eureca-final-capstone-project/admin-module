package eureca.capstone.project.admin.dashboard.service.impl;


import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.dashboard.dto.response.CarrierPriceDto;
import eureca.capstone.project.admin.dashboard.dto.response.DashboardResponseDto;
import eureca.capstone.project.admin.dashboard.dto.response.HourlyPriceStatDto;
import eureca.capstone.project.admin.dashboard.dto.response.HourlyVolumeStatDto;
import eureca.capstone.project.admin.dashboard.service.DashboardService;
import eureca.capstone.project.admin.market_statistic.domain.MarketStatistic;
import eureca.capstone.project.admin.market_statistic.repository.MarketStatisticRepository;
import eureca.capstone.project.admin.report.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ReportHistoryRepository reportHistoryRepository;
    private final MarketStatisticRepository marketStatisticsRepository;
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

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        List<MarketStatistic> recentStats = marketStatisticsRepository.findAllByStaticsTimeAfter(twentyFourHoursAgo);

        // 3. 조회된 통계를 '시간(statisticsTime)'을 기준으로 그룹화
        Map<LocalDateTime, List<MarketStatistic>> statsGroupedByTime = recentStats.stream()
                .collect(Collectors.groupingBy(MarketStatistic::getStaticsTime));

        // 4. DTO로 변환할 리스트 초기화
        List<HourlyPriceStatDto> priceStatsDtoList = new ArrayList<>();
        List<HourlyVolumeStatDto> volumeStatsDtoList = new ArrayList<>();

        // 5. 그룹화된 데이터를 순회하며 DTO 생성
        for (Map.Entry<LocalDateTime, List<MarketStatistic>> entry : statsGroupedByTime.entrySet()) {
            LocalDateTime time = entry.getKey();
            List<MarketStatistic> hourlyStats = entry.getValue();

            // 시세 통계 DTO 생성
            List<CarrierPriceDto> pricesByCarrier = hourlyStats.stream()
                    .map(stat -> CarrierPriceDto.builder()
                            .carrierName(stat.getTelecomCompany().getName()) // TelecomCompany 엔티티의 getName() 호출
                            .pricePerGb(stat.getAveragePrice())
                            .build())
                    .toList();

            priceStatsDtoList.add(HourlyPriceStatDto.builder()
                    .date(time.toLocalDate().toString())
                    .hour(time.getHour())
                    .pricesByCarrier(pricesByCarrier)
                    .build());

            // 거래량 통계 DTO 생성
            long totalVolume = hourlyStats.stream()
                    .mapToLong(MarketStatistic::getTransactionAmount)
                    .sum();

            volumeStatsDtoList.add(HourlyVolumeStatDto.builder()
                    .date(time.toLocalDate().toString())
                    .hour(time.getHour())
                    .generalSaleVolume(totalVolume) // 현재 스키마로는 구분이 불가하여 전체 거래량을 일반판매로 가정
                    .auctionSaleVolume(0)           // 입찰판매는 0으로 설정
                    .build());
        }

        priceStatsDtoList.sort(Comparator.comparing(HourlyPriceStatDto::getDate)
                .thenComparing(HourlyPriceStatDto::getHour));
        volumeStatsDtoList.sort(Comparator.comparing(HourlyVolumeStatDto::getDate)
                .thenComparing(HourlyVolumeStatDto::getHour));

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
