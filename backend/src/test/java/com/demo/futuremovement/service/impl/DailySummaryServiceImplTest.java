package com.demo.futuremovement.service.impl;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import com.demo.futuremovement.service.DailySummaryService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DailySummaryServiceImplTest {

    private final DailySummaryService service = new DailySummaryServiceImpl();

    private ProcessedFutureMovement movement(String clientNumber, String accountNumber, String symbol,
                                              long qtyLong, long qtyShort) {
        return new ProcessedFutureMovement(
                "CL", clientNumber, accountNumber, "0001",
                "SGX", "FU", symbol, LocalDate.of(2010, 9, 10),
                "JPY", "B",
                BigDecimal.valueOf(qtyLong), BigDecimal.valueOf(qtyShort),
                LocalDate.of(2010, 8, 20)
        );
    }

    @Test
    void netsMultipleTransactionsForTheSameClientAndProduct() {
        List<ProcessedFutureMovement> movements = List.of(
                movement("4321", "0002", "NK", 5, 0),
                movement("4321", "0002", "NK", 2, 1),
                movement("4321", "0002", "NK", 0, 3)
        );

        List<DailySummaryRecord> summary = service.summarize(movements);

        assertThat(summary).hasSize(1);
        assertThat(summary.get(0).totalTransactionAmount()).isEqualByComparingTo("3"); // (5+2+0) - (0+1+3)
    }

    @Test
    void keepsDifferentClientsSeparate() {
        List<ProcessedFutureMovement> movements = List.of(
                movement("4321", "0002", "NK", 5, 0),
                movement("1234", "0002", "NK", 5, 0)
        );

        List<DailySummaryRecord> summary = service.summarize(movements);

        assertThat(summary).hasSize(2);
        assertThat(summary).extracting(DailySummaryRecord::clientInformation)
                .containsExactlyInAnyOrder("CL-4321-0002-0001", "CL-1234-0002-0001");
    }

    @Test
    void keepsDifferentProductsForTheSameClientSeparate() {
        List<ProcessedFutureMovement> movements = List.of(
                movement("4321", "0002", "NK", 5, 0),
                movement("4321", "0002", "N1", 5, 0)
        );

        List<DailySummaryRecord> summary = service.summarize(movements);

        assertThat(summary).hasSize(2);
        assertThat(summary).extracting(DailySummaryRecord::productInformation)
                .containsExactlyInAnyOrder("SGX-FU-NK-20100910", "SGX-FU-N1-20100910");
    }

    @Test
    void netTotalCanBeNegative() {
        List<ProcessedFutureMovement> movements = List.of(
                movement("4321", "0002", "NK", 1, 5)
        );

        List<DailySummaryRecord> summary = service.summarize(movements);

        assertThat(summary.get(0).totalTransactionAmount()).isEqualByComparingTo("-4");
    }

    @Test
    void emptyInputProducesEmptySummary() {
        assertThat(service.summarize(List.of())).isEmpty();
    }
}
