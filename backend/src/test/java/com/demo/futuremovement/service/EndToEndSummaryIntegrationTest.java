package com.demo.futuremovement.service;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.mapper.ProcessedFutureMovementMapper;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import com.demo.futuremovement.parser.FixedWidthFileReader;
import com.demo.futuremovement.parser.FixedWidthLineParser;
import com.demo.futuremovement.parser.FixedWidthLineSource;
import com.demo.futuremovement.parser.RecordSchemaRegistry;
import com.demo.futuremovement.service.impl.DailySummaryServiceImpl;
import com.demo.futuremovement.testsupport.ProcessedFutureMovementSchemaFixture;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Runs read -> parse -> map -> aggregate over the real Input.txt, so the
 * numbers are pinned against actual data rather than fixtures. Totals were
 * cross-checked outside the app (see README).
 */
class EndToEndSummaryIntegrationTest {

    @Test
    void endToEndAggregationMatchesIndependentlyComputedTotals() {
        RecordSchemaRegistry registry = new RecordSchemaRegistry();
        registry.setSchemas(Map.of("315", ProcessedFutureMovementSchemaFixture.schema()));

        FixedWidthLineParser lineParser = new FixedWidthLineParser();
        FixedWidthFileReader fileReader = new FixedWidthFileReader(lineParser, registry, new FixedWidthLineSource());
        ProcessedFutureMovementMapper mapper = new ProcessedFutureMovementMapper();
        DailySummaryService summaryService = new DailySummaryServiceImpl();

        List<Map<String, String>> rawRecords = fileReader.readAll(new ClassPathResource("data/Input.txt"));
        List<ProcessedFutureMovement> movements = rawRecords.stream().map(mapper::map).toList();
        List<DailySummaryRecord> summary = summaryService.summarize(movements);

        assertThat(movements).hasSize(717);

        assertThat(summary).extracting(
                        DailySummaryRecord::clientInformation,
                        DailySummaryRecord::productInformation,
                        r -> r.totalTransactionAmount().stripTrailingZeros())
                .containsExactlyInAnyOrder(
                        tuple("CL-1234-0002-0001", "SGX-FU-NK-20100910", new BigDecimal("-52").stripTrailingZeros()),
                        tuple("CL-1234-0003-0001", "CME-FU-N1-20100910", new BigDecimal("285").stripTrailingZeros()),
                        tuple("CL-1234-0003-0001", "CME-FU-NK.-20100910", new BigDecimal("-215").stripTrailingZeros()),
                        tuple("CL-4321-0002-0001", "SGX-FU-NK-20100910", new BigDecimal("46").stripTrailingZeros()),
                        tuple("CL-4321-0003-0001", "CME-FU-N1-20100910", new BigDecimal("-79").stripTrailingZeros())
                );
    }
}
