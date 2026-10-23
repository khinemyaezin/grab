package com.saleschannel.adapter.persistence.repository.config;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import com.saleschannel.adapter.persistence.entity.PublishableKeyJpaEntity;
import com.saleschannel.adapter.persistence.mapper.PublishableKeyEntityMapper;
import com.saleschannel.adapter.persistence.repository.jpa.PublishableKeyJpaRepository;
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
@ContextConfiguration(classes = PublishableKeyRepositoryTestConfig.ContextConfig.class)
@Transactional
public class PublishableKeyRepositoryTestConfig extends PostgreSqlTestContainer {
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = PublishableKeyJpaEntity.class)
    @EnableJpaRepositories(basePackageClasses = PublishableKeyJpaRepository.class)
    @ComponentScan(basePackageClasses = PublishableKeyEntityMapper.class)
    static class ContextConfig {
        @Bean
        IdGenerator idGenerator() {
            return new UuidGenerator();
        }

        @Bean
        IdMapper idMapper(IdGenerator ids) {
            return new IdMapper(ids);
        }
    }
}
