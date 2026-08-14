package com.demo.futuremovement.kafka;

import com.demo.futuremovement.exception.FixedWidthParseException;
import com.demo.futuremovement.mapper.ProcessedFutureMovementMapper;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import com.demo.futuremovement.parser.FixedWidthFileReader;
import com.demo.futuremovement.service.DailyAggregateStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes transaction messages from Kafka and updates the daily aggregate.
 * Invalid messages are logged and skipped so they do not block other messages.
 */
@Component
public class FutureMovementKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(FutureMovementKafkaListener.class);

    private final FixedWidthFileReader fileReader;
    private final ProcessedFutureMovementMapper mapper;
    private final DailyAggregateStoreService aggregateStore;

    public FutureMovementKafkaListener(FixedWidthFileReader fileReader,
                                        ProcessedFutureMovementMapper mapper,
                                        DailyAggregateStoreService aggregateStore) {
        this.fileReader = fileReader;
        this.mapper = mapper;
        this.aggregateStore = aggregateStore;
    }

    @KafkaListener(topics = "${futuremovement.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String rawLine) {
        try {
            Map<String, String> fields = fileReader.parseLine(rawLine);
            ProcessedFutureMovement movement = mapper.map(fields);
            aggregateStore.accumulate(movement);
        } catch (FixedWidthParseException e) {
            log.warn("Skipping malformed future movement message: {}", e.getMessage());
        }
    }
}
