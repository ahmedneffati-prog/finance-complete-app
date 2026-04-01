package com.app.finance.utils;

import com.app.finance.dto.request.AggregationRequestDTO;
import com.app.finance.dto.request.MeasureDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class QueryBuilder {

    private static final java.util.Set<String> ALLOWED_FUNCTIONS =
            java.util.Set.of("SUM", "AVG", "MAX", "MIN", "COUNT");

    private static final java.util.Set<String> ALLOWED_TRADE_FIELDS =
            java.util.Set.of("symbol", "tradeType", "quantity", "price", "totalValue",
                    "tradeDate", "brokerId", "stockId");

    private static final java.util.Set<String> ALLOWED_MARKET_FIELDS =
            java.util.Set.of("symbol", "open", "high", "low", "close", "volume",
                    "changeValue", "changePercent", "interval", "timestamp");

    public String buildJpqlForTrades(AggregationRequestDTO request) {
        StringBuilder sb = new StringBuilder("SELECT ");
        List<String> selectParts = new ArrayList<>();
        List<String> groupByParts = new ArrayList<>();

        if (request.getDimensions() != null) {
            for (String dim : request.getDimensions()) {
                String safeField = sanitizeField(dim);
                selectParts.add("t." + safeField);
                groupByParts.add("t." + safeField);
            }
        }

        if (request.getMeasures() != null) {
            for (MeasureDTO m : request.getMeasures()) {
                String fn = sanitizeFunction(m.getFunction());
                String field = sanitizeField(m.getField());
                selectParts.add(fn + "(t." + field + ")");
            }
        }

        sb.append(String.join(", ", selectParts));
        sb.append(" FROM Trade t");

        List<String> conditions = buildConditions(request, "t");
        if (!conditions.isEmpty()) {
            sb.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        if (!groupByParts.isEmpty()) {
            sb.append(" GROUP BY ").append(String.join(", ", groupByParts));
        }

        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            sb.append(" ORDER BY t.").append(sanitizeField(request.getSortBy().get(0)));
            sb.append(" ").append("DESC".equalsIgnoreCase(request.getSortDirection()) ? "DESC" : "ASC");
        }

        log.debug("Built JPQL: {}", sb);
        return sb.toString();
    }

    private List<String> buildConditions(AggregationRequestDTO request, String alias) {
        List<String> conditions = new ArrayList<>();
        if (request.getStartDate() != null) {
            conditions.add(alias + ".tradeDate >= :startDate");
        }
        if (request.getEndDate() != null) {
            conditions.add(alias + ".tradeDate <= :endDate");
        }
        if (request.getFilters() != null) {
            for (Map.Entry<String, Object> entry : request.getFilters().entrySet()) {
                String field = sanitizeField(entry.getKey());
                conditions.add(alias + "." + field + " = :" + field);
            }
        }
        return conditions;
    }

    private String sanitizeField(String field) {
        if (field == null || !field.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid field name: " + field);
        }
        return field;
    }

    private String sanitizeFunction(String function) {
        String upper = function != null ? function.toUpperCase() : "";
        if (!ALLOWED_FUNCTIONS.contains(upper)) {
            throw new IllegalArgumentException("Invalid aggregate function: " + function);
        }
        return upper;
    }
}
