package com.demo.futuremovement.service;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.model.ProcessedFutureMovement;

import java.util.List;

public interface DailySummaryService {

    List<DailySummaryRecord> summarize(List<ProcessedFutureMovement> movements);
}
