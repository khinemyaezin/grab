package com.grab.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.modulith.Modulithic;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
		"com.grab.app",
		"com.catalog",
		"com.inventory",
		"com.identity",
		"com.merchant",
		"com.pricing",
		"com.saleschannel",
		"com.discovery",
		"com.cart",
		"com.region",
		"com.customer"
})
@Modulithic(useFullyQualifiedModuleNames = true)
@EnableScheduling
@EnableAsync
@EnableRetry(proxyTargetClass = true, order = Ordered.HIGHEST_PRECEDENCE)
public class EcommerceApplication {
	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
	}
}
