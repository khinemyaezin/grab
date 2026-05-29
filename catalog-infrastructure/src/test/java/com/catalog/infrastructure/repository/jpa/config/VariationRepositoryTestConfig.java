package com.catalog.infrastructure.repository.jpa.config;

import com.catalog.infrastructure.repository.jpa.VariantOptionJpaRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.support.DefaultJpaContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@DataJpaTest(excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableAutoConfiguration
@ContextConfiguration(classes = VariationRepositoryTestConfig.ContextConfig.class)
@Transactional
public class VariationRepositoryTestConfig extends PostgreSqlTestContainer{

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.catalog.infrastructure.entity")
    static class ContextConfig {
        @Bean
        EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
            return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        }

        @Bean
        JpaContext jpaContext(EntityManager entityManager) {
            return new DefaultJpaContext(Set.of(entityManager));
        }

        @Bean
        VariantOptionJpaRepo  variantOptionJpaRepo(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(VariantOptionJpaRepo.class);
        }
    }
}
