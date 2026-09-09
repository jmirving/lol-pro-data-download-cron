package com.jmirving.prodata.download;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProDataDownloadCronApplication {
    public static void main(String[] args) {
        configureStructuredOutput(args);
        SpringApplication.run(ProDataDownloadCronApplication.class, args);
    }

    private static void configureStructuredOutput(String[] args) {
        String environmentValue = System.getenv("PRODATA_DOWNLOAD_STRUCTURED_OUTPUT");
        boolean jsonRequested = environmentValue != null && environmentValue.equalsIgnoreCase("json");
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--prodata.download.structuredOutput=json")) {
                jsonRequested = true;
                break;
            }
        }
        if (jsonRequested) {
            System.setProperty("spring.main.banner-mode", "off");
            System.setProperty("logging.level.root", "OFF");
        }
    }
}
