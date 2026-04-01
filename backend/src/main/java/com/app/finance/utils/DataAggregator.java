package com.app.finance.utils;

import com.app.finance.dto.request.AggregationRequestDTO;
import com.app.finance.dto.request.MeasureDTO;
import com.app.finance.dto.response.AggregationResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DataAggregator {

    public AggregationResultDTO aggregate(List<Map<String, Object>> rawData, AggregationRequestDTO request) {
        if (rawData == null || rawData.isEmpty()) {
            return AggregationResultDTO.builder()
                    .dimensions(request.getDimensions())
                    .measures(request.getMeasures().stream().map(m -> m.getAlias() != null ? m.getAlias() : m.getFunction() + "_" + m.getField()).collect(Collectors.toList()))
                    .rows(Collections.emptyList())
                    .totals(new HashMap<>())
                    .totalRows(0)
                    .build();
        }

        List<String> dimensions = request.getDimensions() != null ? request.getDimensions() : Collections.emptyList();
        List<MeasureDTO> measures = request.getMeasures() != null ? request.getMeasures() : Collections.emptyList();

        // Group by dimensions
        Map<String, List<Map<String, Object>>> grouped = rawData.stream()
                .collect(Collectors.groupingBy(row ->
                        dimensions.stream()
                                .map(d -> String.valueOf(row.getOrDefault(d, "")))
                                .collect(Collectors.joining("|"))
                ));

        // Compute aggregates per group
        List<Map<String, Object>> resultRows = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            Map<String, Object> resultRow = new LinkedHashMap<>();
            String[] keyParts = entry.getKey().split("\\|");
            for (int i = 0; i < dimensions.size(); i++) {
                resultRow.put(dimensions.get(i), i < keyParts.length ? keyParts[i] : "");
            }
            List<Map<String, Object>> groupRows = entry.getValue();
            for (MeasureDTO measure : measures) {
                String alias = measure.getAlias() != null ? measure.getAlias()
                        : measure.getFunction() + "_" + measure.getField();
                resultRow.put(alias, computeAggregate(groupRows, measure));
            }
            resultRows.add(resultRow);
        }

        // Sorting
        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            String sortField = request.getSortBy().get(0);
            boolean desc = "DESC".equalsIgnoreCase(request.getSortDirection());
            resultRows.sort((a, b) -> {
                Comparable<Object> va = (Comparable<Object>) a.get(sortField);
                Object vb = b.get(sortField);
                if (va == null) return desc ? 1 : -1;
                int cmp = va.compareTo(vb);
                return desc ? -cmp : cmp;
            });
        }

        // Limit
        if (request.getLimit() != null && resultRows.size() > request.getLimit()) {
            resultRows = resultRows.subList(0, request.getLimit());
        }

        // Totals
        Map<String, Object> totals = new LinkedHashMap<>();
        for (MeasureDTO measure : measures) {
            String alias = measure.getAlias() != null ? measure.getAlias()
                    : measure.getFunction() + "_" + measure.getField();
            totals.put(alias, computeAggregate(rawData, measure));
        }

        return AggregationResultDTO.builder()
                .dimensions(dimensions)
                .measures(measures.stream().map(m -> m.getAlias() != null ? m.getAlias() : m.getFunction() + "_" + m.getField()).collect(Collectors.toList()))
                .rows(resultRows)
                .totals(totals)
                .totalRows(resultRows.size())
                .build();
    }

    private Object computeAggregate(List<Map<String, Object>> rows, MeasureDTO measure) {
        String field = measure.getField();
        String function = measure.getFunction().toUpperCase();

        if ("COUNT".equals(function)) {
            return (long) rows.size();
        }

        List<BigDecimal> values = rows.stream()
                .map(r -> r.get(field))
                .filter(Objects::nonNull)
                .map(v -> new BigDecimal(v.toString()))
                .collect(Collectors.toList());

        if (values.isEmpty()) return null;

        return switch (function) {
            case "SUM" -> values.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(4, RoundingMode.HALF_UP);
            case "AVG" -> values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
            case "MAX" -> values.stream().max(Comparator.naturalOrder()).orElse(null);
            case "MIN" -> values.stream().min(Comparator.naturalOrder()).orElse(null);
            default -> null;
        };
    }
}
