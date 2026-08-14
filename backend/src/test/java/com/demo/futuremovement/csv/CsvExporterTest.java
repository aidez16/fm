package com.demo.futuremovement.csv;

import com.demo.futuremovement.dto.DailySummaryRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExporterTest {

    private final CsvExporter exporter = new CsvExporter();

    @Test
    void writesHeaderAndRowsInOrder() {
        List<DailySummaryRecord> rows = List.of(
                new DailySummaryRecord("CL-4321-0002-0001", "SGX-FU-NK-20100910", new BigDecimal("46")),
                new DailySummaryRecord("CL-1234-0002-0001", "SGX-FU-NK-20100910", new BigDecimal("-52"))
        );

        String csv = exporter.toCsv(rows);

        assertThat(csv).isEqualTo(
                "Client_Information,Product_Information,Total_Transaction_Amount\r\n" +
                "CL-4321-0002-0001,SGX-FU-NK-20100910,46\r\n" +
                "CL-1234-0002-0001,SGX-FU-NK-20100910,-52\r\n"
        );
    }

    @Test
    void emptyRowsProducesHeaderOnly() {
        String csv = exporter.toCsv(List.of());

        assertThat(csv).isEqualTo("Client_Information,Product_Information,Total_Transaction_Amount\r\n");
    }

    @Test
    void quotesValuesContainingCommas() {
        List<DailySummaryRecord> rows = List.of(
                new DailySummaryRecord("CL,4321", "SGX-FU-NK", BigDecimal.TEN)
        );

        String csv = exporter.toCsv(rows);

        assertThat(csv).contains("\"CL,4321\"");
    }

    @Test
    void escapesEmbeddedQuotes() {
        List<DailySummaryRecord> rows = List.of(
                new DailySummaryRecord("CL\"4321\"", "SGX-FU-NK", BigDecimal.ONE)
        );

        String csv = exporter.toCsv(rows);

        assertThat(csv).contains("\"CL\"\"4321\"\"\"");
    }
}
