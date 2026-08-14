package com.demo.futuremovement.service.impl;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.dto.SummaryGroupKey;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import com.demo.futuremovement.service.DailySummaryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailySummaryServiceImpl implements DailySummaryService {

    @Override
    public List<DailySummaryRecord> summarize(List<ProcessedFutureMovement> movements) {
        Map<SummaryGroupKey, BigDecimal> totals = new LinkedHashMap<>();
        for (ProcessedFutureMovement movement : movements) {
            totals.merge(SummaryGroupKey.of(movement), movement.netQuantity(), BigDecimal::add);
        }
        return DailySummaryRecord.sortedFrom(totals);
    }
}
