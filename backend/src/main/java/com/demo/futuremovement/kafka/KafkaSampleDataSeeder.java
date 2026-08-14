package com.demo.futuremovement.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Publishes the sample input to Kafka when the application starts.
 * Can be disabled when an external producer is used.
 */
@Component
public class KafkaSampleDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(KafkaSampleDataSeeder.class);

    private final FutureMovementProducerService producerService;
    private final ResourceLoader resourceLoader;
    private final String inputFilePath;
    private final boolean seedOnStartup;

    public KafkaSampleDataSeeder(FutureMovementProducerService producerService,
                                 ResourceLoader resourceLoader,
                                 @Value("${futuremovement.input-file-path}") String inputFilePath,
                                 @Value("${futuremovement.kafka.seed-on-startup:true}") boolean seedOnStartup) {
        this.producerService = producerService;
        this.resourceLoader = resourceLoader;
        this.inputFilePath = inputFilePath;
        this.seedOnStartup = seedOnStartup;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedTopicOnStartup() {
        if (!seedOnStartup) {
            log.info("Startup seeding is disabled; waiting for an external producer on the future movement topic");
            return;
        }
        try {
            int published = producerService.publishFile(resourceLoader.getResource(inputFilePath));
            log.info("Seeded the future movement topic with {} records from '{}'", published, inputFilePath);
        } catch (RuntimeException e) {
            // Kafka failure should not prevent the application from starting.
            log.warn("Could not seed the future movement topic from '{}': {}", inputFilePath, e.toString());
        }
    }
}
