package com.jmirving.prodata.download.validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

public class CsvHeaderValidator {
    private static final Set<String> REQUIRED_COLUMNS = Set.of(
            "gameid",
            "league",
            "split",
            "year",
            "date",
            "game",
            "patch",
            "participantid",
            "side",
            "teamid",
            "ban1",
            "ban2",
            "ban3",
            "ban4",
            "ban5",
            "pick1",
            "pick2",
            "pick3",
            "pick4",
            "pick5"
    );

    public void validate(Path csvPath) throws IOException {
        if (csvPath == null || !Files.exists(csvPath)) {
            throw new CsvValidationException("CSV path does not exist");
        }
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new CsvValidationException("CSV header row is missing");
            }
            headerLine = stripBom(headerLine);
            List<String> headers = parseHeader(headerLine);
            Set<String> normalized = normalizeHeaders(headers);
            List<String> missing = findMissing(normalized);
            if (!missing.isEmpty()) {
                throw new CsvValidationException("Missing required columns: " + String.join(", ", missing));
            }
        }
    }

    private List<String> parseHeader(String headerLine) throws IOException {
        try (CSVParser parser = CSVParser.parse(headerLine, CSVFormat.DEFAULT)) {
            var iterator = parser.iterator();
            if (!iterator.hasNext()) {
                return List.of();
            }
            List<String> headers = new ArrayList<>();
            for (String value : iterator.next()) {
                headers.add(value);
            }
            return headers;
        }
    }

    private Set<String> normalizeHeaders(List<String> headers) {
        Set<String> normalized = new HashSet<>();
        for (String header : headers) {
            if (header != null) {
                normalized.add(header.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    private List<String> findMissing(Set<String> normalizedHeaders) {
        List<String> missing = new ArrayList<>();
        for (String required : REQUIRED_COLUMNS) {
            if (!normalizedHeaders.contains(required)) {
                missing.add(required);
            }
        }
        return missing;
    }

    private String stripBom(String headerLine) {
        if (headerLine.startsWith("\uFEFF")) {
            return headerLine.substring(1);
        }
        return headerLine;
    }
}
