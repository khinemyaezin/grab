package com.grab.outbox.infrastructure;

import com.grab.outbox.infrastructure.adapter.out.jpa.OutboxRowFactory;
import com.grab.outbox.infrastructure.adapter.out.jpa.claim.ConditionalUpdateClaimStrategy;
import com.grab.outbox.infrastructure.adapter.out.jpa.claim.OutboxClaimStrategy;
import com.grab.outbox.infrastructure.contract.OutboxClaimStrategyContract;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * H2 cannot skip locked rows, so only the portable strategy is configured (and tested) for it.
 */
@ContextConfiguration(classes = ConditionalUpdateClaimH2Test.Config.class)
class ConditionalUpdateClaimH2Test extends OutboxClaimStrategyContract<ClaimFixture> {

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = ClaimFixture.class)
    static class Config {
    }

    @Override
    protected Class<ClaimFixture> entityType() {
        return ClaimFixture.class;
    }

    @Override
    protected OutboxRowFactory<ClaimFixture> rowFactory() {
        return ClaimFixture::pending;
    }


    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:outbox-claim;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Override
    protected OutboxClaimStrategy strategy() {
        return new ConditionalUpdateClaimStrategy();
    }
}
