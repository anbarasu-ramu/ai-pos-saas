package com.anbu.aipos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiPosApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiPosApplication.class, args);
    }
}
