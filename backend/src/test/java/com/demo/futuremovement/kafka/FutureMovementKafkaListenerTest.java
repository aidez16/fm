package com.demo.futuremovement.kafka;

import com.demo.futuremovement.exception.FixedWidthParseException;
import com.demo.futuremovement.mapper.ProcessedFutureMovementMapper;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import com.demo.futuremovement.parser.FixedWidthFileReader;
import com.demo.futuremovement.service.DailyAggregateStoreService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FutureMovementKafkaListenerTest {

    private final FixedWidthFileReader fileReader = mock(FixedWidthFileReader.class);
    private final ProcessedFutureMovementMapper mapper = mock(ProcessedFutureMovementMapper.class);
    private final DailyAggregateStoreService aggregateStore = mock(DailyAggregateStoreService.class);
    private final FutureMovementKafkaListener listener =
            new FutureMovementKafkaListener(fileReader, mapper, aggregateStore);

    @Test
    void parsesMapsAndAccumulatesAValidMessage() {
        String rawLine = "315CL  432100020001...";
        Map<String, String> fields = Map.of("CLIENT_TYPE", "CL");
        ProcessedFutureMovement movement = new ProcessedFutureMovement(
                "CL", "4321", "0002", "0001", "SGX", "FU", "NK", LocalDate.of(2010, 9, 10),
                "JPY", "B", BigDecimal.ONE, BigDecimal.ZERO, LocalDate.of(2010, 8, 20));

        when(fileReader.parseLine(rawLine)).thenReturn(fields);
        when(mapper.map(fields)).thenReturn(movement);

        listener.onMessage(rawLine);

        verify(aggregateStore).accumulate(movement);
    }

    @Test
    void malformedMessageIsSkippedNotThrown() {
        String badLine = "garbage";
        when(fileReader.parseLine(badLine)).thenThrow(new FixedWidthParseException("no schema for record code"));

        assertThatCode(() -> listener.onMessage(badLine)).doesNotThrowAnyException();

        verify(aggregateStore, never()).accumulate(any());
    }

    @Test
    void mapperFailureIsAlsoSkippedNotThrown() {
        String rawLine = "315somepartialrecord";
        Map<String, String> fields = Map.of("CLIENT_TYPE", "CL");
        when(fileReader.parseLine(rawLine)).thenReturn(fields);
        when(mapper.map(fields)).thenThrow(new FixedWidthParseException("missing required field"));

        assertThatCode(() -> listener.onMessage(rawLine)).doesNotThrowAnyException();

        verify(aggregateStore, never()).accumulate(any());
    }

    @Test
    void oneMalformedMessageDoesNotPreventLaterValidMessagesFromBeingProcessed() {
        String badLine = "garbage";
        String goodLine = "315CL  432100020001...";
        Map<String, String> fields = Map.of("CLIENT_TYPE", "CL");
        ProcessedFutureMovement movement = new ProcessedFutureMovement(
                "CL", "4321", "0002", "0001", "SGX", "FU", "NK", LocalDate.of(2010, 9, 10),
                "JPY", "B", BigDecimal.ONE, BigDecimal.ZERO, LocalDate.of(2010, 8, 20));

        when(fileReader.parseLine(badLine)).thenThrow(new FixedWidthParseException("bad"));
        when(fileReader.parseLine(goodLine)).thenReturn(fields);
        when(mapper.map(fields)).thenReturn(movement);

        listener.onMessage(badLine);
        listener.onMessage(goodLine);

        verify(aggregateStore).accumulate(movement);
    }
}
