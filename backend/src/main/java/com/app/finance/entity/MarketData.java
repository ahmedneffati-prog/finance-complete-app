package com.app.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_data", indexes = {
        @Index(name = "idx_market_data_symbol", columnList = "symbol"),
        @Index(name = "idx_market_data_timestamp", columnList = "timestamp"),
        @Index(name = "idx_market_data_stock_id", columnList = "stock_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "open", precision = 20, scale = 4)
    private BigDecimal open;

    @Column(name = "high", precision = 20, scale = 4)
    private BigDecimal high;

    @Column(name = "low", precision = 20, scale = 4)
    private BigDecimal low;

    @Column(name = "close", precision = 20, scale = 4)
    private BigDecimal close;

    @Column(name = "adjusted_close", precision = 20, scale = 4)
    private BigDecimal adjustedClose;

    @Column(name = "volume", precision = 20, scale = 4)
    private BigDecimal volume;

    @Column(name = "change_value", precision = 20, scale = 4)
    private BigDecimal changeValue;

    @Column(name = "change_percent", precision = 10, scale = 4)
    private BigDecimal changePercent;

    @Column(name = "previous_close", precision = 20, scale = 4)
    private BigDecimal previousClose;

    @Column(name = "interval", length = 10)
    private String interval;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Stock stock;
}
