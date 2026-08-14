package com.demo.futuremovement.csv;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.service.DailySummaryService;
import com.demo.futuremovement.service.FutureMovementIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates the daily summary CSV when the application starts.
 */
@Component
@ConditionalOnProperty(prefix = "futuremovement.output", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SummaryCsvFileWriter {

    private static final Logger log = LoggerFactory.getLogger(SummaryCsvFileWriter.class);

    private final FutureMovementIngestionService ingestionService;
    private final DailySummaryService summaryService;
    private final CsvExporter csvExporter;
    private final Path outputPath;

    public SummaryCsvFileWriter(FutureMovementIngestionService ingestionService,
                                DailySummaryService summaryService,
                                CsvExporter csvExporter,
                                @Value("${futuremovement.output.file-path}") String outputFilePath) {
        this.ingestionService = ingestionService;
        this.summaryService = summaryService;
        this.csvExporter = csvExporter;
        this.outputPath = Path.of(outputFilePath);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void writeOnStartup() {
        try {
            write();
        } catch (RuntimeException | IOException e) {
            log.warn("Could not write the daily summary CSV to '{}': {}",
                    outputPath.toAbsolutePath().normalize(), e.toString());
        }
    }

    /**
     * Generates the summary and writes it to the configured file.
     */
    public Path write() throws IOException {
        List<DailySummaryRecord> rows = summaryService.summarize(ingestionService.loadTodaysMovements());
        Path target = outputPath.toAbsolutePath().normalize();

        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(target, csvExporter.toCsv(rows), StandardCharsets.UTF_8);

        log.info("Wrote daily summary report ({} rows) to {}", rows.size(), target);
        return target;
    }
}
