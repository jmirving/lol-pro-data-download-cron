package com.jmirving.prodata.download.select;

import java.time.Clock;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jmirving.prodata.download.provider.RemoteFile;

public class YearFileSelector {
    private static final Pattern YEAR_PREFIX_PATTERN = Pattern.compile("^(\\d{4})_");
    private final Clock clock;

    public YearFileSelector(Clock clock) {
        this.clock = clock;
    }

    public List<RemoteFile> select(List<RemoteFile> files, List<Integer> years) {
        List<Integer> targetYears = resolveTargetYears(years);
        Map<Integer, RemoteFile> byYear = mapByYear(files);
        List<RemoteFile> selected = new ArrayList<>();
        for (Integer year : targetYears) {
            RemoteFile file = byYear.get(year);
            if (file != null) {
                selected.add(file);
            }
        }
        return selected;
    }

    public List<Integer> resolveTargetYears(List<Integer> years) {
        if (years != null && !years.isEmpty()) {
            return years;
        }
        int currentYear = Year.now(clock).getValue();
        return List.of(currentYear, currentYear - 1);
    }

    private Map<Integer, RemoteFile> mapByYear(List<RemoteFile> files) {
        Map<Integer, RemoteFile> byYear = new HashMap<>();
        for (RemoteFile file : files) {
            Integer year = extractYear(file.name());
            if (year != null && !byYear.containsKey(year)) {
                byYear.put(year, file);
            }
        }
        return byYear;
    }

    private Integer extractYear(String filename) {
        if (Objects.isNull(filename)) {
            return null;
        }
        Matcher matcher = YEAR_PREFIX_PATTERN.matcher(filename);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
}
