package com.demo.futuremovement.kafka;

import com.demo.futuremovement.parser.FixedWidthLineSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Publishes fixed-width transaction lines to Kafka.
 */
@Service
public class FutureMovementProducerService {

    private static final Logger log = LoggerFactory.getLogger(FutureMovementProducerService.class);
    private static final int CLIENT_KEY_LENGTH = 19; // RECORD_CODE(3) + CLIENT_TYPE(4) + CLIENT_NUMBER(4) + ACCOUNT_NUMBER(4) + SUBACCOUNT_NUMBER(4)

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FixedWidthLineSource lineSource;
    private final String topic;

    public FutureMovementProducerService(KafkaTemplate<String, String> kafkaTemplate,
                                          FixedWidthLineSource lineSource,
                                          @Value("${futuremovement.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.lineSource = lineSource;
        this.topic = topic;
    }

    /**
     * Publishes each input line as a separate Kafka message.
     *
     * @return number of lines published
     */
    public int publishFile(Resource resource) {
        List<String> lines = lineSource.readLines(resource);
        for (String line : lines) {
            publishLine(line);
        }
        log.info("Published {} future movement records to topic '{}'", lines.size(), topic);
        return lines.size();
    }

    public void publishLine(String rawLine) {
        String key = rawLine.length() >= CLIENT_KEY_LENGTH ? rawLine.substring(0, CLIENT_KEY_LENGTH) : rawLine;
        kafkaTemplate.send(topic, key, rawLine);
    }
}
