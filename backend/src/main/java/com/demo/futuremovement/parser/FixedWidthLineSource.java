package com.demo.futuremovement.parser;

import com.demo.futuremovement.exception.FixedWidthParseException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads non-blank lines from a file.
 */
@Component
public class FixedWidthLineSource {

    public List<String> readLines(Resource resource) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new FixedWidthParseException("Unable to read input file: " + resource, e);
        }
        return lines;
    }
}
