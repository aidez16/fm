package com.demo.futuremovement.service.impl;

import com.demo.futuremovement.exception.FixedWidthParseException;
import com.demo.futuremovement.mapper.ProcessedFutureMovementMapper;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import com.demo.futuremovement.parser.FieldDefinition;
import com.demo.futuremovement.parser.FixedWidthFileReader;
import com.demo.futuremovement.parser.FixedWidthLineParser;
import com.demo.futuremovement.parser.FixedWidthLineSource;
import com.demo.futuremovement.parser.RecordSchema;
import com.demo.futuremovement.parser.RecordSchemaRegistry;
import com.demo.futuremovement.service.FutureMovementIngestionService;
import com.demo.futuremovement.testsupport.ProcessedFutureMovementSchemaFixture;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class FutureMovementIngestionServiceImplTest {

    @Test
    void loadsAndMapsEveryRecordInTheConfiguredFile() {
        RecordSchemaRegistry registry = new RecordSchemaRegistry();
        registry.setSchemas(Map.of("315", ProcessedFutureMovementSchemaFixture.schema()));
        FixedWidthFileReader fileReader = new FixedWidthFileReader(new FixedWidthLineParser(), registry, new FixedWidthLineSource());
        ProcessedFutureMovementMapper mapper = new ProcessedFutureMovementMapper();

        String line = "315CL  432100020001SGXDC FUSGX NK    20100910JPY01B 0000000001 0000000000000000000060DUSD000000000030DUSD000000000000DJPY201008200012380     688032000092500000000             O";

        Resource resource = new ByteArrayResource(line.getBytes(StandardCharsets.UTF_8));
        ResourceLoader resourceLoader = org.mockito.Mockito.mock(ResourceLoader.class);
        when(resourceLoader.getResource("classpath:data/Input.txt")).thenReturn(resource);

        FutureMovementIngestionService service =
                new FutureMovementIngestionServiceImpl(fileReader, mapper, resourceLoader, "classpath:data/Input.txt");

        List<ProcessedFutureMovement> movements = service.loadTodaysMovements();

        assertThat(movements).hasSize(1);
        assertThat(movements.get(0).clientNumber()).isEqualTo("4321");
        assertThat(movements.get(0).symbol()).isEqualTo("NK");
    }

    @Test
    void propagatesParseFailuresForMalformedRecords() {
        RecordSchema schema = new RecordSchema();
        schema.setName("TEST");
        schema.setFields(List.of(new FieldDefinition("RECORD_CODE", 1, 3), new FieldDefinition("REQUIRED_FIELD", 4, 20)));
        RecordSchemaRegistry registry = new RecordSchemaRegistry();
        registry.setSchemas(Map.of("315", schema));
        FixedWidthFileReader fileReader = new FixedWidthFileReader(new FixedWidthLineParser(), registry, new FixedWidthLineSource());
        ProcessedFutureMovementMapper mapper = new ProcessedFutureMovementMapper();

        // Has none of the fields the mapper requires.
        Resource resource = new ByteArrayResource("315somefieldvalue".getBytes(StandardCharsets.UTF_8));
        ResourceLoader resourceLoader = org.mockito.Mockito.mock(ResourceLoader.class);
        when(resourceLoader.getResource("classpath:data/Input.txt")).thenReturn(resource);

        FutureMovementIngestionService service =
                new FutureMovementIngestionServiceImpl(fileReader, mapper, resourceLoader, "classpath:data/Input.txt");

        assertThatThrownBy(service::loadTodaysMovements).isInstanceOf(FixedWidthParseException.class);
    }

    @Test
    void loadMovementsOverloadAcceptsAnArbitraryResource() {
        RecordSchemaRegistry registry = new RecordSchemaRegistry();
        registry.setSchemas(Map.of("315", ProcessedFutureMovementSchemaFixture.schema()));
        FixedWidthFileReader fileReader = new FixedWidthFileReader(new FixedWidthLineParser(), registry, new FixedWidthLineSource());
        ProcessedFutureMovementMapper mapper = new ProcessedFutureMovementMapper();
        ResourceLoader resourceLoader = org.mockito.Mockito.mock(ResourceLoader.class);

        FutureMovementIngestionService service =
                new FutureMovementIngestionServiceImpl(fileReader, mapper, resourceLoader, "classpath:data/Input.txt");

        String line = "315CL  432100020001SGXDC FUSGX NK    20100910JPY01B 0000000001 0000000000000000000060DUSD000000000030DUSD000000000000DJPY201008200012380     688032000092500000000             O";
        Resource adHocResource = new ByteArrayResource(line.getBytes(StandardCharsets.UTF_8));

        List<ProcessedFutureMovement> movements = service.loadMovements(adHocResource);

        assertThat(movements).hasSize(1);
        org.mockito.Mockito.verifyNoInteractions(resourceLoader);
    }
}
