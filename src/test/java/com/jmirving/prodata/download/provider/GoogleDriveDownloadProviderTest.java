package com.jmirving.prodata.download.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class GoogleDriveDownloadProviderTest {
    @Test
    void extractsConfirmTokenFromHtml() {
        String html = "<a href=\"https://drive.google.com/uc?export=download&confirm=t-AB_12&id=123\">download</a>";

        String token = GoogleDriveDownloadProvider.extractConfirmToken(html);

        assertEquals("t-AB_12", token);
    }

    @Test
    void extractsConfirmTokenFromHtmlFormInput() {
        String html = "<form action=\"/uc\"><input type=\"hidden\" name=\"confirm\" value=\"t\"/></form>";

        String token = GoogleDriveDownloadProvider.extractConfirmToken(html);

        assertEquals("t", token);
    }

    @Test
    void extractsConfirmTokenFromCookies() {
        List<String> cookies = List.of(
                "download_warning_123=token-987; Path=/; Secure; HttpOnly",
                "other_cookie=value; Path=/"
        );

        String token = GoogleDriveDownloadProvider.extractConfirmTokenFromCookies(cookies);

        assertEquals("token-987", token);
    }

    @Test
    void extractsDownloadFormFields() {
        String html = "<form action=\"https://drive.usercontent.google.com/download\" method=\"get\">" +
                "<input type=\"hidden\" name=\"id\" value=\"file-id\"/>" +
                "<input type=\"hidden\" name=\"confirm\" value=\"t\"/>" +
                "</form>";

        GoogleDriveDownloadProvider.DownloadForm form = GoogleDriveDownloadProvider.extractDownloadForm(html);

        assertEquals("https://drive.usercontent.google.com/download", form.action());
        assertEquals("file-id", form.parameters().get("id"));
        assertEquals("t", form.parameters().get("confirm"));
    }

    @Test
    void returnsNullWhenNoConfirmTokenPresent() {
        String html = "<html><body>no token here</body></html>";

        String token = GoogleDriveDownloadProvider.extractConfirmToken(html);

        assertNull(token);
    }
}
