package com.grab.service.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.modulith.Modulithic;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Modulithic(sharedModules = {"internal", "shared"})
@SpringBootApplication(scanBasePackages = "com.grab.service.account")
@EnableScheduling
@EnableRetry(proxyTargetClass = true, order = Ordered.HIGHEST_PRECEDENCE)
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
