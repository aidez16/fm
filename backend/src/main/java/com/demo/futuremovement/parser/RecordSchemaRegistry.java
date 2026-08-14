package com.demo.futuremovement.parser;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Record layouts from {@code record-schemas.yml}, keyed by record code. A new
 * record type is a YAML entry, not a new class.
 */
@Component
@ConfigurationProperties(prefix = "record-schemas")
public class RecordSchemaRegistry {

    private Map<String, RecordSchema> schemas = new LinkedHashMap<>();

    public Map<String, RecordSchema> getSchemas() {
        return schemas;
    }

    public void setSchemas(Map<String, RecordSchema> schemas) {
        this.schemas = schemas;
    }

    public RecordSchema getSchema(String recordCode) {
        RecordSchema schema = schemas.get(recordCode);
        if (schema == null) {
            throw new IllegalArgumentException("No record schema registered for record code '" + recordCode + "'");
        }
        return schema;
    }
}
