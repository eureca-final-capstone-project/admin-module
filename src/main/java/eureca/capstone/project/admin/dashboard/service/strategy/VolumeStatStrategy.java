package eureca.capstone.project.admin.dashboard.service.strategy;

import eureca.capstone.project.admin.dashboard.dto.response.VolumeStatDto;
import eureca.capstone.project.admin.market_statistic.domain.TransactionAmountStatistic;

import java.time.LocalDateTime;
import java.util.List;

public interface VolumeStatStrategy {
    boolean supports(String salesTypeName);
    List<VolumeStatDto> buildVolumeDtos(LocalDateTime start, LocalDateTime end, List<TransactionAmountStatistic> stats);
    String getStatType();
}

