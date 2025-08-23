package com.product.infrastructure.configuration;

import com.nestedset.app.NestedSetNodeRepository;
import com.nestedset.app.config.NestedSetRepositoryConfiguration;
import com.nestedset.app.config.factory.JpaNestedSetNodeRepositoryFactory;
import com.nestedset.app.service.TreeBuilderImpl;
import com.product.infrastructure.entity.category.entity.CategoryEntity;
import com.product.infrastructure.entity.category.factory.CategoryComponentFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaContext;

@Configuration
@Import(DomainConfig.class)
@ComponentScan({"com.product.infrastructure"})
public class ProductInfraConfig {
    @Bean
    public NestedSetRepositoryConfiguration<CategoryEntity,Long> categoryRepositoryConfiguration(JpaContext context) {
        return new NestedSetRepositoryConfiguration<>(context, CategoryEntity.class);
    }
    @Bean
    public NestedSetNodeRepository<CategoryEntity,Long> categoryNodeRepository(NestedSetRepositoryConfiguration<CategoryEntity,Long> configuration) {
        return JpaNestedSetNodeRepositoryFactory.create(configuration, new TreeBuilderImpl<>(new CategoryComponentFactory()));
    }
}
