package com.demo.futuremovement.service.impl;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.service.DailyAggregateStoreService;
import com.demo.futuremovement.service.SummaryProviderService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SummaryProviderServiceImplTest {

    @Test
    void returnsWhateverTheAggregateStoreCurrentlyHolds() {
        DailyAggregateStoreService aggregateStore = mock(DailyAggregateStoreService.class);
        List<DailySummaryRecord> expected = List.of(new DailySummaryRecord("CL-4321-0002-0001", "SGX-FU-NK-20100910", BigDecimal.TEN));
        when(aggregateStore.getSortedRecords()).thenReturn(expected);

        SummaryProviderService provider = new SummaryProviderServiceImpl(aggregateStore);

        assertThat(provider.getSummary()).isEqualTo(expected);
    }

    @Test
    void returnsEmptyWhenNothingConsumedYet() {
        DailyAggregateStoreService aggregateStore = mock(DailyAggregateStoreService.class);
        when(aggregateStore.getSortedRecords()).thenReturn(List.of());

        SummaryProviderService provider = new SummaryProviderServiceImpl(aggregateStore);

        assertThat(provider.getSummary()).isEmpty();
    }
}
