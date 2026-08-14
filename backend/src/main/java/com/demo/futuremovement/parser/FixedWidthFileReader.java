package com.demo.futuremovement.parser;

import com.demo.futuremovement.exception.FixedWidthParseException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads and parses fixed-width records using the schema for each record code.
 */
@Component
public class FixedWidthFileReader {

    private static final int RECORD_CODE_LENGTH = 3;

    private final FixedWidthLineParser lineParser;
    private final RecordSchemaRegistry schemaRegistry;
    private final FixedWidthLineSource lineSource;

    public FixedWidthFileReader(FixedWidthLineParser lineParser,
                                 RecordSchemaRegistry schemaRegistry,
                                 FixedWidthLineSource lineSource) {
        this.lineParser = lineParser;
        this.schemaRegistry = schemaRegistry;
        this.lineSource = lineSource;
    }

    /**
     * Parses all records in the file.
     */
    public List<Map<String, String>> readAll(Resource resource) {
        List<String> lines = lineSource.readLines(resource);
        List<Map<String, String>> records = new ArrayList<>(lines.size());
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            records.add(parseLineOrThrowWithContext(line, lineNumber));
        }
        return records;
    }

    /**
     * Parses a single fixed-width line.
     */
    public Map<String, String> parseLine(String line) {
        if (line.length() < RECORD_CODE_LENGTH) {
            throw new FixedWidthParseException("Line is too short to contain a record code: " + line);
        }
        String recordCode = line.substring(0, RECORD_CODE_LENGTH);
        RecordSchema schema;
        try {
            schema = schemaRegistry.getSchema(recordCode);
        } catch (IllegalArgumentException e) {
            throw new FixedWidthParseException(e.getMessage(), e);
        }
        return lineParser.parse(line, schema);
    }

    private Map<String, String> parseLineOrThrowWithContext(String line, int lineNumber) {
        try {
            return parseLine(line);
        } catch (FixedWidthParseException e) {
            throw new FixedWidthParseException("Failed to parse line " + lineNumber + ": " + e.getMessage(), e);
        }
    }
}
