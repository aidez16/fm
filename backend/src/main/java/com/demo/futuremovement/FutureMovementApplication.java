package com.demo.futuremovement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FutureMovementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FutureMovementApplication.class, args);
    }
}
