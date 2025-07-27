package eureca.capstone.project.admin.dashboard.service.strategy;

import eureca.capstone.project.admin.dashboard.dto.response.VolumeStatDto;
import eureca.capstone.project.admin.market_statistic.domain.TransactionAmountStatistic;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("hourlyStrategy")
public class HourlyVolumeStatStrategy implements VolumeStatStrategy {

    @Override
    public boolean supports(String salesTypeName) {
        return "일반 판매".equals(salesTypeName);
    }

    @Override
    public List<VolumeStatDto> buildVolumeDtos(LocalDateTime start, LocalDateTime end, List<TransactionAmountStatistic> stats) {
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

    @Override
    public String getStatType() {
        return "HOUR";
    }
}

