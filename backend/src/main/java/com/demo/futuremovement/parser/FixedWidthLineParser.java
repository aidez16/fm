package com.demo.futuremovement.parser;

import com.demo.futuremovement.exception.FixedWidthParseException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses a fixed-width line using the provided schema.
 */
@Component
public class FixedWidthLineParser {

    /**
     * Extracts and trims all fields from the line.
     */
    public Map<String, String> parse(String line, RecordSchema schema) {
        Map<String, String> values = new LinkedHashMap<>();
        for (FieldDefinition field : schema.getFields()) {
            values.put(field.getName(), extract(line, field));
        }
        return values;
    }

    private String extract(String line, FieldDefinition field) {
        int startIndex = field.getStart() - 1; // 1-indexed -> 0-indexed

        // Field is outside the line.
        if (startIndex >= line.length()) {
            return "";
        }

        // Field is only partially present.
        if (line.length() < field.getEnd()) {
            throw new FixedWidthParseException(
                    "Line too short for field '" + field.getName() + "' (expected through position "
                            + field.getEnd() + ", line has " + line.length() + " characters): " + line);
        }

        return line.substring(startIndex, field.getEnd()).trim();
    }
}
