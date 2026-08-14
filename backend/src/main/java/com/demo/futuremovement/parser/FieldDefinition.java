package com.demo.futuremovement.parser;

/**
 * One column of a fixed-width layout. {@code start} and {@code end} are
 * 1-indexed and inclusive, matching the file specification.
 */
public class FieldDefinition {

    private String name;
    private int start;
    private int end;

    public FieldDefinition() {
        // for YAML binding
    }

    public FieldDefinition(String name, int start, int end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int length() {
        return end - start + 1;
    }
}
