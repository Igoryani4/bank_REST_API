package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.bankcards")
@EntityScan("com.example.bankcards.entity")
@EnableJpaRepositories("com.example.bankcards.repository")
public class BankRestApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankRestApplication.class, args);
    }
}
