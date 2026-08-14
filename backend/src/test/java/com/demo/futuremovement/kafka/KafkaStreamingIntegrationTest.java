package com.demo.futuremovement.kafka;

import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.service.DailyAggregateStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the real context against an embedded broker and checks the whole path
 * wires together: topic config, listener, parsing, mapping, aggregation.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "futuremovement.kafka.topic=future-movements-test",
                // This test asserts exact totals from its own lines, so the
                // seeder must not pour the sample file on top.
                "futuremovement.kafka.seed-on-startup=false",
                // Don't rewrite the repo's sample-output/Output.csv.
                "futuremovement.output.enabled=false",
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.consumer.group-id=future-movement-summary-test"
        })
@EmbeddedKafka(partitions = 1, topics = "future-movements-test")
class KafkaStreamingIntegrationTest {

    // A real record 315 line.
    private static final String VALID_LINE =
            "315CL  432100020001SGXDC FUSGX NK    20100910JPY01B 0000000001 0000000000000000000060DUSD000000000030DUSD000000000000DJPY201008200012380     688032000092500000000             O";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private DailyAggregateStoreService aggregateStore;

    @BeforeEach
    void resetAggregateStore() {
        aggregateStore.reset();
    }

    @Test
    void publishedMessageIsConsumedAndAppearsInTheAggregate() throws InterruptedException {
        kafkaTemplate.send("future-movements-test", "key1", VALID_LINE);

        List<DailySummaryRecord> records = awaitNonEmptyRecords();

        assertThat(records).hasSize(1);
        assertThat(records.get(0).clientInformation()).isEqualTo("CL-4321-0002-0001");
        assertThat(records.get(0).totalTransactionAmount()).isEqualByComparingTo("1");
    }

    @Test
    void multipleMessagesForTheSameGroupAreNettedTogether() throws InterruptedException {
        kafkaTemplate.send("future-movements-test", "key1", VALID_LINE);
        kafkaTemplate.send("future-movements-test", "key1", VALID_LINE);

        List<DailySummaryRecord> records = awaitRecordsWithTotal("2");

        assertThat(records).hasSize(1);
        assertThat(records.get(0).totalTransactionAmount()).isEqualByComparingTo("2");
    }

    @Test
    void malformedMessageIsSkippedAndDoesNotBlockLaterValidMessages() throws InterruptedException {
        kafkaTemplate.send("future-movements-test", "bad-key", "not a valid fixed width record");
        kafkaTemplate.send("future-movements-test", "key1", VALID_LINE);

        List<DailySummaryRecord> records = awaitNonEmptyRecords();

        assertThat(records).hasSize(1);
        assertThat(records.get(0).totalTransactionAmount()).isEqualByComparingTo("1");
    }

    private List<DailySummaryRecord> awaitNonEmptyRecords() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        List<DailySummaryRecord> records = aggregateStore.getSortedRecords();
        while (records.isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(200);
            records = aggregateStore.getSortedRecords();
        }
        return records;
    }

    private List<DailySummaryRecord> awaitRecordsWithTotal(String expectedTotal) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        List<DailySummaryRecord> records = aggregateStore.getSortedRecords();
        while (System.currentTimeMillis() < deadline) {
            records = aggregateStore.getSortedRecords();
            if (!records.isEmpty() && records.get(0).totalTransactionAmount().compareTo(new java.math.BigDecimal(expectedTotal)) == 0) {
                break;
            }
            Thread.sleep(200);
        }
        return records;
    }
}
