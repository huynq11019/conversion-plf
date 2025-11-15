package com.example.docformatting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DocFormattingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocFormattingPlatformApplication.class, args);
    }
}
