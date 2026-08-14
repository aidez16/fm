package com.demo.futuremovement;

import com.demo.futuremovement.service.SummaryProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every Kafka bean is registered unconditionally, so an unreachable broker
 * must still leave a working application. Points the client at a dead port
 * and checks the context comes up and serves an empty summary.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.kafka.bootstrap-servers=localhost:59999",
                "spring.kafka.admin.fail-fast=false",
                // Otherwise the seeder's first send() blocks for the 60s default
                // max.block.ms waiting for metadata that never arrives.
                "spring.kafka.producer.properties.max.block.ms=2000",
                "spring.kafka.properties.request.timeout.ms=2000",
                // Don't rewrite the repo's sample-output/Output.csv.
                "futuremovement.output.enabled=false"
        })
class ApplicationStartsWithoutBrokerTest {

    @Autowired
    private SummaryProviderService summaryProvider;

    @Test
    void contextStartsAndServesAnEmptySummaryWhenNoBrokerIsReachable() {
        assertThat(summaryProvider).isNotNull();
        assertThat(summaryProvider.getSummary()).isEmpty();
    }
}
