package com.identity.infrastructure.repository.jpa.config;

import com.identity.infrastructure.repository.jpa.*;
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
    @EntityScan(basePackages = {
            "com.identity.infrastructure.entity",
            "com.identity.infrastructure.outbox"
    })
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
        AuthorityJpaRepository getAuthorityJpaRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(AuthorityJpaRepository.class);
        }

        @Bean
        ExternalEntitlementMappingJpaRepository getExternalEntitlementMappingJpaRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(ExternalEntitlementMappingJpaRepository.class);
        }

        @Bean
        ExternalIdentityJpaRepository getExternalIdentityJpaRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(ExternalIdentityJpaRepository.class);
        }

        @Bean
        IdentityOutboxEventJpaRepo getIdentityOutboxEventJpaRepo(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(IdentityOutboxEventJpaRepo.class);
        }

        @Bean
        RefreshSessionJpaRepository getRefreshSessionJpaRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(RefreshSessionJpaRepository.class);
        }

        @Bean
        RoleJpaRepository getRoleJpaRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(RoleJpaRepository.class);
        }

        @Bean
        RoleDelegationRuleJpaRepository getRoleDelegationRuleJpaRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(RoleDelegationRuleJpaRepository.class);
        }

        @Bean
        UserJpaRepository getUserJpaRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(UserJpaRepository.class);
        }

        @Bean
        UserQueryRepository getUserQueryRepository(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(UserQueryRepository.class);
        }
    }
}
