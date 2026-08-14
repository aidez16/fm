package com.demo.futuremovement.service;

import com.demo.futuremovement.dto.DailySummaryRecord;

import java.util.List;

public interface SummaryProviderService {

    List<DailySummaryRecord> getSummary();
}
