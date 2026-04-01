package com.app.finance.repository;

import com.app.finance.entity.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {

    List<MarketData> findBySymbolOrderByTimestampDesc(String symbol);

    List<MarketData> findBySymbolAndTimestampBetweenOrderByTimestampAsc(
            String symbol, LocalDateTime start, LocalDateTime end);

    Optional<MarketData> findTopBySymbolOrderByTimestampDesc(String symbol);

    @Query("SELECT m FROM MarketData m WHERE m.symbol = :symbol AND m.interval = :interval " +
           "AND m.timestamp BETWEEN :start AND :end ORDER BY m.timestamp ASC")
    List<MarketData> findBySymbolIntervalAndDateRange(
            @Param("symbol") String symbol,
            @Param("interval") String interval,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT m.symbol, MAX(m.close), MIN(m.close), AVG(m.close), SUM(m.volume) " +
           "FROM MarketData m WHERE m.timestamp BETWEEN :start AND :end GROUP BY m.symbol")
    List<Object[]> findSummaryByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    void deleteBySymbolAndTimestampBefore(String symbol, LocalDateTime cutoff);
}
