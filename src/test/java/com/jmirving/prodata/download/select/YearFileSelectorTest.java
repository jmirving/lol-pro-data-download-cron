package com.jmirving.prodata.download.select;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jmirving.prodata.download.provider.RemoteFile;
import org.junit.jupiter.api.Test;

class YearFileSelectorTest {
    @Test
    void defaultsToCurrentAndPreviousYearWhenYearsEmpty() {
        Clock clock = Clock.fixed(
                LocalDate.of(2026, 1, 15).atStartOfDay(ZoneOffset.UTC).toInstant(),
                ZoneOffset.UTC
        );
        YearFileSelector selector = new YearFileSelector(clock);

        RemoteFile file2025 = new RemoteFile("id2025", "2025_LoL_esports_match_data_from_OraclesElixir.csv");
        RemoteFile file2026 = new RemoteFile("id2026", "2026_LoL_esports_match_data_from_OraclesElixir.csv");
        List<RemoteFile> files = List.of(file2025, file2026);

        Set<RemoteFile> selected = new HashSet<>(selector.select(files, List.of()));

        assertEquals(Set.of(file2025, file2026), selected);
    }

    @Test
    void selectsExplicitYearsWhenProvided() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC);
        YearFileSelector selector = new YearFileSelector(clock);

        RemoteFile file2024 = new RemoteFile("id2024", "2024_LoL_esports_match_data_from_OraclesElixir.csv");
        RemoteFile file2026 = new RemoteFile("id2026", "2026_LoL_esports_match_data_from_OraclesElixir.csv");
        List<RemoteFile> files = List.of(file2024, file2026);

        List<RemoteFile> selected = selector.select(files, List.of(2024));

        assertEquals(List.of(file2024), selected);
    }
}
