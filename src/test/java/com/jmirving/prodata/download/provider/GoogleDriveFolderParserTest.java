package com.jmirving.prodata.download.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class GoogleDriveFolderParserTest {
    @Test
    void parsesCsvFilesFromHtml() {
        String html = "[[null,&quot;fileId2025&quot;],null,null,null,&quot;text/csv&quot;" +
                ",null,null,null,null,null,null,true,null,null,null,[[2]],null,null,null,null,null,null,null,null," +
                "[[[&quot;&quot;],[null,[&quot;text/csv&quot;]],null,null,null,&quot;CSV&quot;],null,[[16,null,[null,[[[&quot;2025_LoL_esports_match_data_from_OraclesElixir.csv&quot;,null,true]]]]]]]]" +
                "...[[null,&quot;fileId2026&quot;],null,null,null,&quot;text/csv&quot;" +
                ",null,null,null,null,null,null,true,null,null,null,[[2]],null,null,null,null,null,null,null,null," +
                "[[[&quot;&quot;],[null,[&quot;text/csv&quot;]],null,null,null,&quot;CSV&quot;],null,[[16,null,[null,[[[&quot;2026_LoL_esports_match_data_from_OraclesElixir.csv&quot;,null,true]]]]]]]]";

        List<RemoteFile> files = GoogleDriveFolderParser.parse(html);

        Set<RemoteFile> expected = Set.of(
                new RemoteFile("fileId2025", "2025_LoL_esports_match_data_from_OraclesElixir.csv"),
                new RemoteFile("fileId2026", "2026_LoL_esports_match_data_from_OraclesElixir.csv")
        );
        assertEquals(expected, new HashSet<>(files));
    }
}
