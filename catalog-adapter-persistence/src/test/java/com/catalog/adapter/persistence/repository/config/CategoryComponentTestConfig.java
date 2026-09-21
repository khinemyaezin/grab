package com.catalog.adapter.persistence.repository.config;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.adapter.persistence.category.CategoryNodeInserter;
import com.catalog.adapter.persistence.category.CategoryNodeRemover;
import com.catalog.adapter.persistence.category.CategoryNodeRetriever;
import com.catalog.adapter.persistence.category.impl.CategoryNodeInserterImpl;
import com.catalog.adapter.persistence.category.impl.CategoryNodeRemoverImpl;
import com.catalog.adapter.persistence.category.impl.CategoryNodeRetrieverImpl;
import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.factory.CategoryComponentFactory;
import com.catalog.adapter.persistence.repository.*;
import com.catalog.adapter.persistence.adapter.CategoryJpaInsertingDelegateImpl;
import com.catalog.adapter.persistence.adapter.CategoryJpaRemovingDelegateImpl;
import com.catalog.adapter.persistence.adapter.CategoryJpaRetrievingDelegateImpl;
import com.catalog.adapter.persistence.adapter.CategoryNestedSetNodeRepositoryImpl;
import com.catalog.adapter.persistence.adapter.CategoryNodeRepositoryImpl;
import com.catalog.adapter.persistence.adapter.CategoryQueryAdapter;
import com.catalog.adapter.persistence.adapter.CatalogPersistenceExecutor;
import com.nestedset.app.config.JpaNestedSetRepositoryConfiguration;
import com.nestedset.app.service.TreeBuilder;
import com.nestedset.app.service.TreeBuilderImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.support.DefaultJpaContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

import java.util.Set;

@EnableAutoConfiguration
@EntityScan(basePackages = "com.catalog.adapter.persistence.entity")
public class CategoryComponentTestConfig {

    @Bean
    EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    JpaContext jpaContext(EntityManager entityManager) {
        return new DefaultJpaContext(Set.of(entityManager));
    }

    @Bean
    CategoryComponentFactory categoryComponentFactory() {
        return new CategoryComponentFactory();
    }

    @Bean
    JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> categoryNestedSetConfig(JpaContext jpaContext) {
        return new JpaNestedSetRepositoryConfiguration<>(jpaContext, CategoryEntity.class);
    }

    @Bean
    TreeBuilder<CategoryEntity, Long> categoryTreeBuilder(CategoryComponentFactory categoryComponentFactory) {
        return new TreeBuilderImpl<>(categoryComponentFactory);
    }

    @Bean
    CategoryJpaRepo categoryJpaRepo(EntityManager entityManager) {
        return new JpaRepositoryFactory(entityManager).getRepository(CategoryJpaRepo.class);
    }

    @Bean
    VariantTypeJpaRepo variantTypeJpaRepo(EntityManager entityManager) {
        return new JpaRepositoryFactory(entityManager).getRepository(VariantTypeJpaRepo.class);
    }

    @Bean
    VariantOptionJpaRepo variantOptionJpaRepo(EntityManager entityManager) {
        return new JpaRepositoryFactory(entityManager).getRepository(VariantOptionJpaRepo.class);
    }

    @Bean
    CategoryJpaInsertingDelegate categoryJpaInsertingDelegate(
            JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config
    ) {
        return new CategoryJpaInsertingDelegateImpl(config);
    }

    @Bean
    CategoryJpaRemovingDelegate categoryJpaRemovingDelegate(
            JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config
    ) {
        return new CategoryJpaRemovingDelegateImpl(config);
    }

    @Bean
    CategoryJpaRetrievingDelegate categoryJpaRetrievingDelegate(
            JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config
    ) {
        return new CategoryJpaRetrievingDelegateImpl(config);
    }

    @Bean
    CategoryNodeInserter categoryNodeInserter(CategoryJpaInsertingDelegate insertingDelegate) {
        return new CategoryNodeInserterImpl(insertingDelegate);
    }

    @Bean
    CategoryNodeRemover categoryNodeRemover(CategoryJpaRemovingDelegate removingDelegate) {
        return new CategoryNodeRemoverImpl(removingDelegate);
    }

    @Bean
    CategoryNodeRetriever categoryNodeRetriever(CategoryJpaRetrievingDelegate retrievingDelegate) {
        return new CategoryNodeRetrieverImpl(retrievingDelegate);
    }

    @Bean
    CategoryNestedSetNodeRepository categoryNestedSetNodeRepository(
            CategoryNodeInserter categoryNodeInserter,
            CategoryNodeRemover categoryNodeRemover,
            CategoryNodeRetriever categoryNodeRetriever,
            TreeBuilder<CategoryEntity, Long> categoryTreeBuilder,
            CategoryJpaRetrievingDelegate categoryJpaRetrievingDelegate
    ) {
        return new CategoryNestedSetNodeRepositoryImpl(
                categoryNodeInserter,
                categoryNodeRemover,
                categoryNodeRetriever,
                categoryTreeBuilder,
                categoryJpaRetrievingDelegate
        );
    }

    @Bean
    CategoryNodeRepository categoryNodeRepository(
            CategoryNestedSetNodeRepository nestedSetNodeRepository,
            CategoryJpaRepo categoryJpaRepo
    ) {
        return new CategoryNodeRepositoryImpl(categoryJpaRepo, nestedSetNodeRepository);
    }

    @Bean
    CategoryQueryPort categoryQueryRepository(
            CategoryJpaRepo categoryJpaRepo,
            CategoryNodeRepository categoryNodeRepository
    ) {
        return new CategoryQueryAdapter(categoryJpaRepo, categoryNodeRepository, new CatalogPersistenceExecutor());
    }
}
