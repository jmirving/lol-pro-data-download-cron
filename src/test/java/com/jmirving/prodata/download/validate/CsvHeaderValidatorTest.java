package com.jmirving.prodata.download.validate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvHeaderValidatorTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsHeaderOnlyCsv() throws IOException {
        Path csv = tempDir.resolve("header-only.csv");
        Files.writeString(csv, "gameid,league,split,year,date,game,patch,participantid,side,teamid,ban1,ban2,ban3,ban4,ban5,pick1,pick2,pick3,pick4,pick5\n");

        CsvHeaderValidator validator = new CsvHeaderValidator();

        assertDoesNotThrow(() -> validator.validate(csv));
    }

    @Test
    void failsWhenMissingRequiredColumns() throws IOException {
        Path csv = tempDir.resolve("missing-columns.csv");
        Files.writeString(csv, "gameid,league,split,year,date\n");

        CsvHeaderValidator validator = new CsvHeaderValidator();

        assertThrows(CsvValidationException.class, () -> validator.validate(csv));
    }

    @Test
    void handlesBomInHeader() throws IOException {
        Path csv = tempDir.resolve("bom.csv");
        Files.writeString(csv, "\uFEFFgameid,league,split,year,date,game,patch,participantid,side,teamid,ban1,ban2,ban3,ban4,ban5,pick1,pick2,pick3,pick4,pick5\n");

        CsvHeaderValidator validator = new CsvHeaderValidator();

        assertDoesNotThrow(() -> validator.validate(csv));
    }
}
