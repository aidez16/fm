package com.demo.futuremovement.controller;

import com.demo.futuremovement.kafka.FutureMovementProducerService;
import com.demo.futuremovement.service.DailyAggregateStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Replays the sample data to Kafka.
 * Clears the current aggregate first to avoid duplicate totals.
 */
@RestController
@RequestMapping("/api/v1/future-movements")
public class ReplayController {

    private final FutureMovementProducerService producerService;
    private final DailyAggregateStoreService aggregateStore;
    private final ResourceLoader resourceLoader;
    private final String inputFilePath;

    public ReplayController(FutureMovementProducerService producerService,
                             DailyAggregateStoreService aggregateStore,
                             ResourceLoader resourceLoader,
                             @Value("${futuremovement.input-file-path}") String inputFilePath) {
        this.producerService = producerService;
        this.aggregateStore = aggregateStore;
        this.resourceLoader = resourceLoader;
        this.inputFilePath = inputFilePath;
    }

    @PostMapping("/replay-sample-data")
    public ResponseEntity<Map<String, Object>> replaySampleData() {
        aggregateStore.reset();
        int published = producerService.publishFile(resourceLoader.getResource(inputFilePath));
        return ResponseEntity.ok(Map.of("publishedRecords", published));
    }
}
