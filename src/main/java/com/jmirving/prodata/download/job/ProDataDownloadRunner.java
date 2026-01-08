package com.jmirving.prodata.download.job;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ProDataDownloadRunner implements ApplicationRunner {
    private final ProDataDownloadJob job;

    public ProDataDownloadRunner(ProDataDownloadJob job) {
        this.job = job;
    }

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = job.run();
        System.exit(exitCode);
    }
}
