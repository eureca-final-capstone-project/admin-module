package eureca.capstone.project.admin.market_statistic.repository;

import eureca.capstone.project.admin.market_statistic.domain.MarketStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketStatisticRepository extends JpaRepository<MarketStatistic, Long> {
    @Query("select ms from MarketStatistic ms join fetch ms.telecomCompany tc " +
            "where ms.staticsTime >= :from and ms.staticsTime < :to " +
            "order by ms.staticsTime asc")
    List<MarketStatistic> findAllByStaticsTimeRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
