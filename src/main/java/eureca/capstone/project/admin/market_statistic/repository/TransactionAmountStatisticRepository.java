package eureca.capstone.project.admin.market_statistic.repository;

import eureca.capstone.project.admin.market_statistic.domain.MarketStatistic;
import eureca.capstone.project.admin.market_statistic.domain.TransactionAmountStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionAmountStatisticRepository extends JpaRepository<TransactionAmountStatistic, Long> {
    @Query("select ts from TransactionAmountStatistic ts " +
            "where ts.staticsTime >= :from and ts.staticsTime < :to " +
            "and ts.salesType.salesTypeId = :salesType " +
            "and ts.statisticType = :statType " +
            "order by ts.staticsTime asc")
    List<TransactionAmountStatistic> findAllByStaticsTimeRange(
            @Param("salesType") Long salesType,
            @Param("statType") String statType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

}
