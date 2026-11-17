package com.grab.service.sellerbff;

import com.grab.rpc.adapter.RmiClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = SessionAutoConfiguration.class)
@Import(RmiClientConfiguration.class)
public class SellerBffApplication {
    public static void main(String[] args) {
        SpringApplication.run(SellerBffApplication.class, args);
    }
}
