package com.demo.futuremovement.parser;

import java.util.List;

/** The fixed-width layout for one record type, e.g. "315" = PROCESSED FUTURE MOVEMENT. */
public class RecordSchema {

    private String name;
    private int totalLength;
    private List<FieldDefinition> fields;

    public RecordSchema() {
        // for YAML binding
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public void setFields(List<FieldDefinition> fields) {
        this.fields = fields;
    }
}
