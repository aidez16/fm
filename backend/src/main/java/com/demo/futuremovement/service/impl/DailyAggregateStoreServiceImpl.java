package com.demo.futuremovement.service.impl;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.dto.SummaryGroupKey;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import com.demo.futuremovement.service.DailyAggregateStoreService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DailyAggregateStoreServiceImpl implements DailyAggregateStoreService {

    private final Map<SummaryGroupKey, BigDecimal> totals = new ConcurrentHashMap<>();

    @Override
    public void accumulate(ProcessedFutureMovement movement) {
        totals.merge(SummaryGroupKey.of(movement), movement.netQuantity(), BigDecimal::add);
    }

    @Override
    public List<DailySummaryRecord> getSortedRecords() {
        return DailySummaryRecord.sortedFrom(totals);
    }

    @Override
    public int groupCount() {
        return totals.size();
    }

    @Override
    public void reset() {
        totals.clear();
    }
}
