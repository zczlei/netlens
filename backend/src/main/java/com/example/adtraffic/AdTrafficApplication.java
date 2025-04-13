package com.example.adtraffic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdTrafficApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdTrafficApplication.class, args);
    }
} 