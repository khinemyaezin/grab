package com.product.infrastructure.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DomainConfig.class)
@ComponentScan({"com.product.infrastructure"})
public class ProductInfraConfig {

}
