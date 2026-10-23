package com.pricing.adapter.persistence.repository.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.support.PersistenceExecutor;
import com.pricing.adapter.persistence.adapter.PricingPersistenceExecutor;
import com.pricing.adapter.persistence.entity.PriceEntity;
import com.pricing.adapter.persistence.entity.PriceListEntity;
import com.pricing.adapter.persistence.entity.PriceListRuleEntity;
import com.pricing.adapter.persistence.entity.PricePreferenceEntity;
import com.pricing.adapter.persistence.entity.PriceRuleEntity;
import com.pricing.adapter.persistence.entity.PriceSetEntity;
import com.pricing.adapter.persistence.entity.VariantPriceSetLinkEntity;
import com.pricing.adapter.persistence.repository.jpa.PriceJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PriceListJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PricePreferenceJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PriceSetJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.VariantPriceSetLinkJpaRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class, showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PricingRepositoryTestConfig.ContextConfig.class)
@Transactional
public class PricingRepositoryTestConfig extends PostgreSqlTestContainer {
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {
            PriceEntity.class,
            PriceListEntity.class,
            PriceListRuleEntity.class,
            PricePreferenceEntity.class,
            PriceRuleEntity.class,
            PriceSetEntity.class,
            VariantPriceSetLinkEntity.class
    })
    @EnableJpaRepositories(basePackageClasses = {
            PriceJpaRepository.class,
            PriceListJpaRepository.class,
            PricePreferenceJpaRepository.class,
            PriceSetJpaRepository.class,
            VariantPriceSetLinkJpaRepository.class
    })
    @ComponentScan(basePackageClasses = PricingRepositoryTestConfig.class)
    static class ContextConfig {
        @Bean
        IdGenerator idGenerator() {
            return new UuidGenerator();
        }

        @Bean
        IdMapper idMapper(IdGenerator idGenerator) {
            return new IdMapper(idGenerator);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        PersistenceExecutor persistenceExecutor() {
            return new PricingPersistenceExecutor();
        }
    }
}
