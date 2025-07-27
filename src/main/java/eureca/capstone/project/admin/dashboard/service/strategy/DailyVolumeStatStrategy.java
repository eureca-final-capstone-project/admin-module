package eureca.capstone.project.admin.dashboard.service.strategy;

import eureca.capstone.project.admin.dashboard.dto.response.VolumeStatDto;
import eureca.capstone.project.admin.market_statistic.domain.TransactionAmountStatistic;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("dailyStrategy")
public class DailyVolumeStatStrategy implements VolumeStatStrategy {

    @Override
    public boolean supports(String salesTypeName) {
        return "입찰 판매".equals(salesTypeName);
    }

    @Override
    public List<VolumeStatDto> buildVolumeDtos(LocalDateTime start, LocalDateTime end, List<TransactionAmountStatistic> stats) {
        Map<LocalDate, Long> map = stats.stream()
                .collect(Collectors.toMap(s -> s.getStaticsTime().toLocalDate(),
                        TransactionAmountStatistic::getTransactionAmount));
        List<VolumeStatDto> result = new ArrayList<>();
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate().minusDays(1);
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            result.add(VolumeStatDto.builder()
                    .date(d.toString())
                    .hour(null)
                    .saleVolume(map.getOrDefault(d, 0L))
                    .build());
        }
        return result;
    }

    @Override
    public String getStatType() {
        return "DAY";
    }
}

