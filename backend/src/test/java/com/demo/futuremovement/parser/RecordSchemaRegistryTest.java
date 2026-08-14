package com.demo.futuremovement.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecordSchemaRegistryTest {

    @Test
    void returnsSchemaForKnownRecordCode() {
        RecordSchemaRegistry registry = new RecordSchemaRegistry();
        RecordSchema schema = new RecordSchema();
        schema.setName("PROCESSED_FUTURE_MOVEMENT");
        schema.setFields(List.of(new FieldDefinition("RECORD_CODE", 1, 3)));
        registry.setSchemas(Map.of("315", schema));

        assertThat(registry.getSchema("315")).isSameAs(schema);
    }

    @Test
    void throwsForUnregisteredRecordCode() {
        RecordSchemaRegistry registry = new RecordSchemaRegistry();
        registry.setSchemas(Map.of());

        assertThatThrownBy(() -> registry.getSchema("999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("999");
    }

    @Test
    void defaultsToEmptyMapWhenNeverConfigured() {
        RecordSchemaRegistry registry = new RecordSchemaRegistry();

        assertThat(registry.getSchemas()).isEmpty();
    }
}
