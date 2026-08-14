package com.demo.futuremovement.dto;

import com.demo.futuremovement.model.ProcessedFutureMovement;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SummaryGroupKeyTest {

    private ProcessedFutureMovement movement(String clientNumber, String symbol) {
        return new ProcessedFutureMovement(
                "CL", clientNumber, "0002", "0001",
                "SGX", "FU", symbol, LocalDate.of(2010, 9, 10),
                "JPY", "B", BigDecimal.ONE, BigDecimal.ZERO, LocalDate.of(2010, 8, 20));
    }

    @Test
    void ofDerivesKeyFromMovementClientAndProductInformation() {
        SummaryGroupKey key = SummaryGroupKey.of(movement("4321", "NK"));

        assertThat(key.clientInformation()).isEqualTo("CL-4321-0002-0001");
        assertThat(key.productInformation()).isEqualTo("SGX-FU-NK-20100910");
    }

    @Test
    void keysForTheSameClientAndProductAreEqual() {
        SummaryGroupKey a = SummaryGroupKey.of(movement("4321", "NK"));
        SummaryGroupKey b = SummaryGroupKey.of(movement("4321", "NK"));

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void keysForDifferentClientsAreNotEqual() {
        SummaryGroupKey a = SummaryGroupKey.of(movement("4321", "NK"));
        SummaryGroupKey b = SummaryGroupKey.of(movement("1234", "NK"));

        assertThat(a).isNotEqualTo(b);
    }
}
