package com.demo.futuremovement.kafka;

import com.demo.futuremovement.parser.FixedWidthLineSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FutureMovementProducerServiceTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final FixedWidthLineSource lineSource = mock(FixedWidthLineSource.class);
    private final FutureMovementProducerService producerService =
            new FutureMovementProducerService(kafkaTemplate, lineSource, "future-movements");

    @Test
    void publishesEveryLineToTheConfiguredTopic() {
        Resource resource = mock(Resource.class);
        String line1 = "315CL  43210002000100000000000000000000000000000000000000";
        String line2 = "315CL  12340002000100000000000000000000000000000000000000";
        when(lineSource.readLines(resource)).thenReturn(List.of(line1, line2));

        int published = producerService.publishFile(resource);

        assertThat(published).isEqualTo(2);
        verify(kafkaTemplate).send(eq("future-movements"), any(), eq(line1));
        verify(kafkaTemplate).send(eq("future-movements"), any(), eq(line2));
    }

    @Test
    void keysMessageByClientIdentifyingPrefixSoOneClientsRecordsStayOrdered() {
        String line = "315CL  432100020001SGXDC FUSGX NK    20100910JPY01B 0000000001";
        Resource resource = mock(Resource.class);
        when(lineSource.readLines(resource)).thenReturn(List.of(line));

        producerService.publishFile(resource);

        // RECORD_CODE + CLIENT_TYPE + CLIENT_NUMBER + ACCOUNT_NUMBER + SUBACCOUNT_NUMBER = 19 chars
        verify(kafkaTemplate).send("future-movements", "315CL  432100020001", line);
    }

    @Test
    void shortLineUsesTheWholeLineAsKeyRatherThanThrowing() {
        String shortLine = "315CL";
        Resource resource = mock(Resource.class);
        when(lineSource.readLines(resource)).thenReturn(List.of(shortLine));

        producerService.publishFile(resource);

        verify(kafkaTemplate).send("future-movements", shortLine, shortLine);
    }

    @Test
    void emptyFileSendsNothing() {
        Resource resource = mock(Resource.class);
        when(lineSource.readLines(resource)).thenReturn(List.of());

        int published = producerService.publishFile(resource);

        assertThat(published).isZero();
        verify(kafkaTemplate, times(0)).send(any(), any(), any());
    }

    @Test
    void publishLineSendsASingleMessage() {
        String line = "315CL  432100020001SGXDC FUSGX NK    20100910JPY01B 0000000001";

        producerService.publishLine(line);

        verify(kafkaTemplate).send(eq("future-movements"), any(), eq(line));
    }
}
