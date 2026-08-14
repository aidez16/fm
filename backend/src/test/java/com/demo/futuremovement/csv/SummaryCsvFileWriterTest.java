package com.demo.futuremovement.csv;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import com.demo.futuremovement.service.DailySummaryService;
import com.demo.futuremovement.service.FutureMovementIngestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SummaryCsvFileWriterTest {

    @TempDir
    Path tempDir;

    private static final ProcessedFutureMovement MOVEMENT = new ProcessedFutureMovement(
            "CL", "4321", "0002", "0001", "SGX", "FU", "NK", LocalDate.of(2010, 9, 10),
            "JPY", "B", BigDecimal.ONE, BigDecimal.ZERO, LocalDate.of(2010, 8, 20));

    private static final List<DailySummaryRecord> SUMMARY = List.of(
            new DailySummaryRecord("CL-4321-0002-0001", "SGX-FU-NK-20100910", new BigDecimal("46")));

    @Test
    void writesTheSummaryReportToTheConfiguredPath() throws IOException {
        Path target = tempDir.resolve("Output.csv");

        writerFor(target.toString(), SUMMARY).write();

        assertThat(Files.readString(target, StandardCharsets.UTF_8)).isEqualTo(
                "Client_Information,Product_Information,Total_Transaction_Amount\r\n" +
                "CL-4321-0002-0001,SGX-FU-NK-20100910,46\r\n");
    }

    @Test
    void createsMissingParentDirectories() throws IOException {
        Path target = tempDir.resolve("sample-output").resolve("Output.csv");

        writerFor(target.toString(), SUMMARY).write();

        assertThat(target).exists();
    }

    @Test
    void writesTheFileAsSoonAsTheApplicationIsReady() {
        Path target = tempDir.resolve("Output.csv");

        writerFor(target.toString(), SUMMARY).writeOnStartup();

        assertThat(target).exists();
    }

    @Test
    void overwritesAPreviousReportRatherThanAppendingToIt() throws IOException {
        Path target = tempDir.resolve("Output.csv");
        Files.writeString(target, "stale content from an earlier run\r\n");

        writerFor(target.toString(), SUMMARY).write();

        assertThat(Files.readString(target, StandardCharsets.UTF_8)).doesNotContain("stale content");
    }

    /** An unwritable location is a warning, not a startup failure. */
    @Test
    void startupWriteFailureIsSwallowed() throws IOException {
        Path blocker = tempDir.resolve("blocker");
        Files.writeString(blocker, "not a directory");
        Path target = blocker.resolve("nested").resolve("Output.csv");

        SummaryCsvFileWriter writer = writerFor(target.toString(), SUMMARY);

        assertThatCode(writer::writeOnStartup).doesNotThrowAnyException();
    }

    private SummaryCsvFileWriter writerFor(String outputPath, List<DailySummaryRecord> summary) {
        FutureMovementIngestionService ingestionService = mock(FutureMovementIngestionService.class);
        DailySummaryService summaryService = mock(DailySummaryService.class);
        List<ProcessedFutureMovement> movements = List.of(MOVEMENT);

        when(ingestionService.loadTodaysMovements()).thenReturn(movements);
        when(summaryService.summarize(movements)).thenReturn(summary);

        return new SummaryCsvFileWriter(ingestionService, summaryService, new CsvExporter(), outputPath);
    }
}
