package com.demo.futuremovement.service;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.model.ProcessedFutureMovement;

import java.util.List;

public interface DailyAggregateStoreService {

    void accumulate(ProcessedFutureMovement movement);

    /** The totals accumulated so far as report rows, sorted by client then product. */
    List<DailySummaryRecord> getSortedRecords();

    int groupCount();

    void reset();
}
