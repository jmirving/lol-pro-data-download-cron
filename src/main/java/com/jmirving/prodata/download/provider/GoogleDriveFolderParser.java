package com.jmirving.prodata.download.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GoogleDriveFolderParser {
    private static final Pattern CSV_FILE_PATTERN = Pattern.compile(
            "\\[\\[null,\\\"([A-Za-z0-9_-]{6,})\\\"\\],null,null,null,\\\"text/csv\\\".*?\\\"(\\d{4}_LoL_esports_match_data_from_OraclesElixir\\.csv)\\\"",
            Pattern.DOTALL
    );

    private GoogleDriveFolderParser() {
    }

    static List<RemoteFile> parse(String html) {
        String normalized = unescapeHtml(html);
        List<RemoteFile> files = new ArrayList<>();
        Matcher matcher = CSV_FILE_PATTERN.matcher(normalized);
        while (matcher.find()) {
            files.add(new RemoteFile(matcher.group(1), matcher.group(2)));
        }
        return files;
    }

    private static String unescapeHtml(String html) {
        return html.replace("&quot;", "\"").replace("&amp;", "&");
    }
}
