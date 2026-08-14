package com.demo.futuremovement.parser;

import com.demo.futuremovement.exception.FixedWidthParseException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixedWidthLineSourceTest {

    private final FixedWidthLineSource lineSource = new FixedWidthLineSource();

    private Resource resourceOf(String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void readsAllNonBlankLines() {
        List<String> lines = lineSource.readLines(resourceOf("line one\nline two\nline three"));

        assertThat(lines).containsExactly("line one", "line two", "line three");
    }

    @Test
    void skipsBlankAndWhitespaceOnlyLines() {
        List<String> lines = lineSource.readLines(resourceOf("a\n\n   \nb\n"));

        assertThat(lines).containsExactly("a", "b");
    }

    @Test
    void emptyResourceProducesEmptyList() {
        assertThat(lineSource.readLines(resourceOf(""))).isEmpty();
    }

    @Test
    void doesNotTrimOrAlterLineContent() {
        // Trimming/parsing is the parser's job, not the line source's - it must hand back raw lines untouched.
        List<String> lines = lineSource.readLines(resourceOf("  padded value  \n"));

        assertThat(lines).containsExactly("  padded value  ");
    }

    @Test
    void ioExceptionIsWrappedAsFixedWidthParseException() {
        Resource brokenResource = new Resource() {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("disk on fire");
            }

            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public java.net.URL getURL() {
                throw new UnsupportedOperationException();
            }

            @Override
            public java.net.URI getURI() {
                throw new UnsupportedOperationException();
            }

            @Override
            public java.io.File getFile() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long contentLength() {
                return 0;
            }

            @Override
            public long lastModified() {
                return 0;
            }

            @Override
            public Resource createRelative(String relativePath) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getFilename() {
                return "broken.txt";
            }

            @Override
            public String getDescription() {
                return "broken test resource";
            }
        };

        assertThatThrownBy(() -> lineSource.readLines(brokenResource))
                .isInstanceOf(FixedWidthParseException.class)
                .hasCauseInstanceOf(IOException.class);
    }
}
