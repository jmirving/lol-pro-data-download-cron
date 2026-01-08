package com.jmirving.prodata.download.provider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DownloadProvider {
    List<RemoteFile> listFiles() throws IOException, InterruptedException;

    void download(RemoteFile file, Path destination) throws IOException, InterruptedException;

    String sourceUrl(RemoteFile file);
}
