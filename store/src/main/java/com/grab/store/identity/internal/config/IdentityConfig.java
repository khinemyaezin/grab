package com.grab.store.identity.internal.config;

import com.identity.infrastructure.configuration.IdentityInfraConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(IdentityInfraConfig.class)
public class IdentityConfig {
}
