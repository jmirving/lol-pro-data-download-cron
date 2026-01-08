package com.jmirving.prodata.download;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProDataDownloadCronApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProDataDownloadCronApplication.class, args);
    }
}
