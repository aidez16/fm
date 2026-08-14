package com.demo.futuremovement.service.impl;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.service.DailyAggregateStoreService;
import com.demo.futuremovement.service.SummaryProviderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummaryProviderServiceImpl implements SummaryProviderService {

    private final DailyAggregateStoreService aggregateStore;

    public SummaryProviderServiceImpl(DailyAggregateStoreService aggregateStore) {
        this.aggregateStore = aggregateStore;
    }

    @Override
    public List<DailySummaryRecord> getSummary() {
        return aggregateStore.getSortedRecords();
    }
}
