package com.demo.futuremovement.parser;

import com.demo.futuremovement.exception.FixedWidthParseException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixedWidthFileReaderTest {

    private final RecordSchemaRegistry registry = new RecordSchemaRegistry();
    private final FixedWidthFileReader reader =
            new FixedWidthFileReader(new FixedWidthLineParser(), registry, new FixedWidthLineSource());

    FixedWidthFileReaderTest() {
        RecordSchema schema = new RecordSchema();
        schema.setName("TEST");
        schema.setFields(List.of(
                new FieldDefinition("RECORD_CODE", 1, 3),
                new FieldDefinition("VALUE", 4, 8)
        ));
        registry.setSchemas(Map.of("ABC", schema));
    }

    private Resource resourceOf(String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void parsesEveryNonBlankLineInFileOrder() {
        Resource resource = resourceOf("ABC12345\nABC67890\n");

        List<Map<String, String>> records = reader.readAll(resource);

        assertThat(records).hasSize(2);
        assertThat(records.get(0).get("VALUE")).isEqualTo("12345");
        assertThat(records.get(1).get("VALUE")).isEqualTo("67890");
    }

    @Test
    void skipsBlankLines() {
        Resource resource = resourceOf("ABC12345\n\n   \nABC67890\n");

        List<Map<String, String>> records = reader.readAll(resource);

        assertThat(records).hasSize(2);
    }

    @Test
    void emptyFileProducesEmptyList() {
        assertThat(reader.readAll(resourceOf(""))).isEmpty();
    }

    @Test
    void unknownRecordCodeThrows() {
        Resource resource = resourceOf("XYZ12345\n");

        assertThatThrownBy(() -> reader.readAll(resource))
                .isInstanceOf(FixedWidthParseException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    void lineTooShortForRecordCodeThrows() {
        Resource resource = resourceOf("AB\n");

        assertThatThrownBy(() -> reader.readAll(resource))
                .isInstanceOf(FixedWidthParseException.class)
                .hasMessageContaining("record code");
    }

    /**
     * "ABC12" ends partway through the VALUE field (4-8), so it is truncated
     * rather than right-trimmed. "ABC" alone is fine - see
     * {@link #fieldsEntirelyPastTheEndOfTheLineAreEmpty}.
     */
    @Test
    void parseErrorIncludesLineNumberForEasierDebugging() {
        Resource resource = resourceOf("ABC12345\nABC12\n");

        assertThatThrownBy(() -> reader.readAll(resource))
                .isInstanceOf(FixedWidthParseException.class)
                .hasMessageContaining("line 2");
    }

    @Test
    void fieldsEntirelyPastTheEndOfTheLineAreEmpty() {
        Resource resource = resourceOf("ABC\n");

        List<Map<String, String>> records = reader.readAll(resource);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).get("VALUE")).isEmpty();
    }

    @Test
    void parseLineCanBeUsedStandaloneForSingleMessages() {
        Map<String, String> fields = reader.parseLine("ABC99999");

        assertThat(fields.get("VALUE")).isEqualTo("99999");
    }

    @Test
    void parseLineThrowsForUnknownRecordCode() {
        assertThatThrownBy(() -> reader.parseLine("ZZZ99999"))
                .isInstanceOf(FixedWidthParseException.class)
                .hasMessageContaining("ZZZ");
    }
}
