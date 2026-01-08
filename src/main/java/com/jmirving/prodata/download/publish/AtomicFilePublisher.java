package com.jmirving.prodata.download.publish;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AtomicFilePublisher {
    public void publish(Path tempFile, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        Files.move(tempFile, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
}
