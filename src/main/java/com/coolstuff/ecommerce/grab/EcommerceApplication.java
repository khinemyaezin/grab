package com.coolstuff.ecommerce.grab;

import com.coolstuff.ecommerce.grab.auth.impl.AuthenticationServiceImpl;
import com.coolstuff.ecommerce.grab.dto.auth.RegisterRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
	}
}
