package com.grab.service.checkout;

import com.grab.rpc.adapter.RmiClientConfiguration;
import com.grab.rpc.adapter.RmiProviderConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.grab.service.checkout")
@Import({RmiProviderConfiguration.class, RmiClientConfiguration.class})
@EnableScheduling
@EnableRetry(proxyTargetClass = true, order = Ordered.HIGHEST_PRECEDENCE)
public class CheckoutServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckoutServiceApplication.class, args);
    }
}
