package com.demo.futuremovement.dto;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Represents one summary row for a client and product.
 */
public record DailySummaryRecord(
        String clientInformation,
        String productInformation,
        BigDecimal totalTransactionAmount
) {

    /**
     * Converts totals into sorted summary rows.
     */
    public static List<DailySummaryRecord> sortedFrom(Map<SummaryGroupKey, BigDecimal> totals) {
        return totals.entrySet().stream()
                .map(entry -> new DailySummaryRecord(
                        entry.getKey().clientInformation(),
                        entry.getKey().productInformation(),
                        entry.getValue()))
                .sorted(Comparator.comparing(DailySummaryRecord::clientInformation)
                        .thenComparing(DailySummaryRecord::productInformation))
                .toList();
    }
}
