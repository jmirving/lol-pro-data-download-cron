package com.jmirving.prodata.download.job;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmirving.prodata.download.config.ProDataDownloadProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ProDataDownloadRunner implements ApplicationRunner {
    private final ProDataDownloadJob job;
    private final ProDataDownloadProperties properties;
    private final ObjectMapper objectMapper;

    public ProDataDownloadRunner(
            ProDataDownloadJob job,
            ProDataDownloadProperties properties,
            ObjectMapper objectMapper
    ) {
        this.job = job;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        DownloadJobResult result = job.runWithResult();
        if (properties.getStructuredOutput() == ProDataDownloadProperties.StructuredOutput.JSON) {
            objectMapper.writeValue(System.out, result);
            System.out.println();
        }
        System.exit(result.exitCode());
    }
}
