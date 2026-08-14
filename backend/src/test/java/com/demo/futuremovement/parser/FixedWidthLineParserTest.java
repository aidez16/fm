package com.demo.futuremovement.parser;

import com.demo.futuremovement.exception.FixedWidthParseException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixedWidthLineParserTest {

    private final FixedWidthLineParser parser = new FixedWidthLineParser();

    private RecordSchema schema(FieldDefinition... fields) {
        RecordSchema schema = new RecordSchema();
        schema.setName("TEST");
        schema.setFields(List.of(fields));
        return schema;
    }

    @Test
    void extractsAndTrimsEachFieldByPosition() {
        RecordSchema schema = schema(
                new FieldDefinition("CODE", 1, 3),
                new FieldDefinition("NAME", 4, 10)
        );

        Map<String, String> result = parser.parse("315ABC    ", schema);

        assertThat(result).containsExactly(
                Map.entry("CODE", "315"),
                Map.entry("NAME", "ABC")
        );
    }

    @Test
    void blankFieldBecomesEmptyStringNotNull() {
        RecordSchema schema = schema(new FieldDefinition("SPACES", 1, 5));

        Map<String, String> result = parser.parse("     ", schema);

        assertThat(result.get("SPACES")).isEmpty();
    }

    @Test
    void tolerateTrailingWhitespaceHavingBeenStrippedFromTheLine() {
        // Real files right-trim the FILLER block, so a field starting where the
        // line now ends is empty, not an error.
        RecordSchema schema = schema(
                new FieldDefinition("CODE", 1, 3),
                new FieldDefinition("FILLER", 4, 10)
        );

        Map<String, String> result = parser.parse("315", schema);

        assertThat(result.get("CODE")).isEqualTo("315");
        assertThat(result.get("FILLER")).isEmpty();
    }

    @Test
    void throwsWhenLineIsTooShortMidField() {
        RecordSchema schema = schema(new FieldDefinition("CODE", 1, 10));

        assertThatThrownBy(() -> parser.parse("315", schema))
                .isInstanceOf(FixedWidthParseException.class)
                .hasMessageContaining("CODE");
    }
}
