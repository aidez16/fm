package com.demo.futuremovement.controller;

import com.demo.futuremovement.csv.CsvExporter;
import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.service.SummaryProviderService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Provides the daily summary as JSON or CSV.
 * <p>
 * {@code @CrossOrigin} is scoped to the configured frontend origin so the
 * Angular dev server can call this directly.
 */
@RestController
@RequestMapping("/api/v1/future-movements")
@CrossOrigin(origins = "${futuremovement.cors.allowed-origin:http://localhost:4200}")
public class DailySummaryController {

    private final SummaryProviderService summaryProvider;
    private final CsvExporter csvExporter;

    public DailySummaryController(SummaryProviderService summaryProvider, CsvExporter csvExporter) {
        this.summaryProvider = summaryProvider;
        this.csvExporter = csvExporter;
    }

    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DailySummaryRecord> getDailySummaryAsJson() {
        return summaryProvider.getSummary();
    }

    @GetMapping(value = "/summary/csv")
    public ResponseEntity<byte[]> getDailySummaryAsCsv() {
        String csv = csvExporter.toCsv(summaryProvider.getSummary());
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Output.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }
}
