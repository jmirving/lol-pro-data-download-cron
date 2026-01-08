package com.jmirving.prodata.download.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleDriveDownloadProvider implements DownloadProvider {
    private static final Pattern CONFIRM_TOKEN_PATTERN = Pattern.compile("confirm=([0-9A-Za-z_]+)");
    private final HttpClient httpClient;
    private final String folderUrl;
    private final String userAgent;
    private final Duration readTimeout;

    public GoogleDriveDownloadProvider(String folderUrl, String userAgent, Duration connectTimeout, Duration readTimeout) {
        this.folderUrl = folderUrl;
        this.userAgent = userAgent;
        this.readTimeout = readTimeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(new CookieManager())
                .build();
    }

    @Override
    public List<RemoteFile> listFiles() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(folderUrl))
                .header("User-Agent", userAgent)
                .timeout(readTimeout)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("Google Drive listing failed with status " + response.statusCode());
        }
        return GoogleDriveFolderParser.parse(response.body());
    }

    @Override
    public void download(RemoteFile file, Path destination) throws IOException, InterruptedException {
        URI downloadUri = buildDownloadUri(file.id(), Optional.empty());
        HttpRequest request = HttpRequest.newBuilder(downloadUri)
                .header("User-Agent", userAgent)
                .timeout(readTimeout)
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        String contentType = response.headers().firstValue("content-type").orElse("");
        if (response.statusCode() != 200) {
            throw new IOException("Google Drive download failed with status " + response.statusCode());
        }

        if (contentType.contains("text/html")) {
            String body;
            try (InputStream inputStream = response.body()) {
                body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            String confirmToken = extractConfirmToken(body);
            if (confirmToken == null) {
                throw new IOException("Google Drive download confirmation token not found");
            }
            fetchConfirmed(file, confirmToken, destination);
            return;
        }

        try (InputStream inputStream = response.body()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public String sourceUrl(RemoteFile file) {
        return buildDownloadUri(file.id(), Optional.empty()).toString();
    }

    private void fetchConfirmed(RemoteFile file, String confirmToken, Path destination)
            throws IOException, InterruptedException {
        URI confirmedUri = buildDownloadUri(file.id(), Optional.of(confirmToken));
        HttpRequest confirmedRequest = HttpRequest.newBuilder(confirmedUri)
                .header("User-Agent", userAgent)
                .timeout(readTimeout)
                .GET()
                .build();
        HttpResponse<InputStream> confirmedResponse = httpClient.send(
                confirmedRequest,
                HttpResponse.BodyHandlers.ofInputStream()
        );

        if (confirmedResponse.statusCode() != 200) {
            throw new IOException("Google Drive confirmed download failed with status " + confirmedResponse.statusCode());
        }

        try (InputStream inputStream = confirmedResponse.body()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private URI buildDownloadUri(String fileId, Optional<String> confirmToken) {
        StringBuilder uri = new StringBuilder("https://drive.google.com/uc?export=download&id=")
                .append(fileId);
        confirmToken.ifPresent(token -> uri.append("&confirm=").append(token));
        return URI.create(uri.toString());
    }

    private String extractConfirmToken(String body) {
        Matcher matcher = CONFIRM_TOKEN_PATTERN.matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
