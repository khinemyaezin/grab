package com.grab.service.authorizationserver;

import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.spi.LoggerConfig;
import com.grab.service.authorizationserver.config.SellerIssuerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(SellerIssuerProperties.class)
public class AuthorizationServerApplication {
    public static void main(String[] args) {
        Loggers.setConfigLoader(() -> new LoggerConfig("slf4j", null, Map.of(), false));
        SpringApplication.run(AuthorizationServerApplication.class, args);
    }
}
