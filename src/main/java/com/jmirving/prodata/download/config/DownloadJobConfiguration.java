package com.jmirving.prodata.download.config;

import java.time.Clock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmirving.prodata.download.job.ProDataDownloadJob;
import com.jmirving.prodata.download.provider.DownloadProvider;
import com.jmirving.prodata.download.provider.GoogleDriveDownloadProvider;
import com.jmirving.prodata.download.publish.AtomicFilePublisher;
import com.jmirving.prodata.download.publish.ManifestWriter;
import com.jmirving.prodata.download.select.YearFileSelector;
import com.jmirving.prodata.download.validate.CsvHeaderValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DownloadJobConfiguration {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public DownloadProvider downloadProvider(ProDataDownloadProperties properties) {
        return new GoogleDriveDownloadProvider(
                properties.getGoogleDriveFolderUrl(),
                properties.getUserAgent(),
                properties.getConnectTimeout(),
                properties.getReadTimeout()
        );
    }

    @Bean
    public YearFileSelector yearFileSelector(Clock clock) {
        return new YearFileSelector(clock);
    }

    @Bean
    public CsvHeaderValidator csvHeaderValidator() {
        return new CsvHeaderValidator();
    }

    @Bean
    public AtomicFilePublisher atomicFilePublisher() {
        return new AtomicFilePublisher();
    }

    @Bean
    public ManifestWriter manifestWriter(ObjectMapper objectMapper, Clock clock) {
        return new ManifestWriter(objectMapper, clock);
    }

    @Bean
    public ProDataDownloadJob proDataDownloadJob(
            ProDataDownloadProperties properties,
            DownloadProvider downloadProvider,
            YearFileSelector yearFileSelector,
            CsvHeaderValidator csvHeaderValidator,
            AtomicFilePublisher atomicFilePublisher,
            ManifestWriter manifestWriter
    ) {
        return new ProDataDownloadJob(
                properties,
                downloadProvider,
                yearFileSelector,
                csvHeaderValidator,
                atomicFilePublisher,
                manifestWriter
        );
    }
}
