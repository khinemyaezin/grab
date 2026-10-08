package com.merchant.adapter.persistence.repository.jpa.config;

import com.merchant.adapter.persistence.repository.jpa.MerchantAccountJpaRepository;
import com.merchant.adapter.persistence.repository.jpa.StorefrontJpaRepository;
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
@ContextConfiguration(classes = RepositoryTestConfig.ContextConfig.class)
@Transactional
public class RepositoryTestConfig extends PostgreSqlTestContainer {

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.merchant.adapter.persistence.entity")
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
        MerchantAccountJpaRepository merchantAccountJpaRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(MerchantAccountJpaRepository.class);
        }

        @Bean
        StorefrontJpaRepository storefrontJpaRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(StorefrontJpaRepository.class);
        }

        @Bean
        com.merchant.adapter.persistence.repository.jpa.MerchantMemberJpaRepository merchantMemberJpaRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(com.merchant.adapter.persistence.repository.jpa.MerchantMemberJpaRepository.class);
        }
    }
}
