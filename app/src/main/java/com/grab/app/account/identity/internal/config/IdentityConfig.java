package com.grab.app.account.identity.internal.config;

import com.identity.adapter.persistence.configuration.IdentityPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        IdentityUseCaseConfig.class,
        IdentityPersistenceConfig.class,
        IdentityDataSourceConfig.class,
        IdentityModuleOutboxConfig.class,
        IdentitySecurityManifestPublicationConfig.class
})
public class IdentityConfig {
}

