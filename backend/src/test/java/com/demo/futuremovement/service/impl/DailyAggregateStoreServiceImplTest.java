package com.demo.futuremovement.service.impl;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import com.demo.futuremovement.service.DailyAggregateStoreService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DailyAggregateStoreServiceImplTest {

    private final DailyAggregateStoreService store = new DailyAggregateStoreServiceImpl();

    private ProcessedFutureMovement movement(String clientNumber, String symbol, long qtyLong, long qtyShort) {
        return new ProcessedFutureMovement(
                "CL", clientNumber, "0002", "0001",
                "SGX", "FU", symbol, LocalDate.of(2010, 9, 10),
                "JPY", "B", BigDecimal.valueOf(qtyLong), BigDecimal.valueOf(qtyShort), LocalDate.of(2010, 8, 20));
    }

    @Test
    void getSortedRecordsIsEmptyBeforeAnyAccumulation() {
        assertThat(store.getSortedRecords()).isEmpty();
        assertThat(store.groupCount()).isZero();
    }

    @Test
    void accumulatesMultipleMessagesForTheSameGroup() {
        store.accumulate(movement("4321", "NK", 5, 0));
        store.accumulate(movement("4321", "NK", 2, 1));

        List<DailySummaryRecord> records = store.getSortedRecords();

        assertThat(records).hasSize(1);
        assertThat(records.get(0).totalTransactionAmount()).isEqualByComparingTo("6");
    }

    @Test
    void keepsDifferentGroupsSeparate() {
        store.accumulate(movement("4321", "NK", 5, 0));
        store.accumulate(movement("1234", "NK", 3, 0));

        assertThat(store.groupCount()).isEqualTo(2);
    }

    @Test
    void resetClearsAllAccumulatedTotals() {
        store.accumulate(movement("4321", "NK", 5, 0));

        store.reset();

        assertThat(store.getSortedRecords()).isEmpty();
        assertThat(store.groupCount()).isZero();
    }

    @Test
    void getSortedRecordsAreSortedByClientThenProduct() {
        store.accumulate(movement("4321", "NK", 1, 0));
        store.accumulate(movement("1234", "NK", 1, 0));

        List<DailySummaryRecord> records = store.getSortedRecords();

        assertThat(records).extracting(DailySummaryRecord::clientInformation)
                .containsExactly("CL-1234-0002-0001", "CL-4321-0002-0001");
    }

    @Test
    void concurrentAccumulationDoesNotLoseUpdates() throws InterruptedException {
        int threadCount = 8;
        int updatesPerThread = 500;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                for (int i = 0; i < updatesPerThread; i++) {
                    store.accumulate(movement("4321", "NK", 1, 0));
                }
                latch.countDown();
            });
        }

        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        List<DailySummaryRecord> records = store.getSortedRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).totalTransactionAmount())
                .isEqualByComparingTo(BigDecimal.valueOf((long) threadCount * updatesPerThread));
    }
}
