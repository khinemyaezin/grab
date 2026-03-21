package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.adapter.category.CategoryNodeInserter;
import com.catalog.infrastructure.adapter.category.CategoryNodeRemover;
import com.catalog.infrastructure.adapter.category.CategoryNodeRetriever;
import com.catalog.infrastructure.adapter.category.impl.CategoryNodeInserterImpl;
import com.catalog.infrastructure.adapter.category.impl.CategoryNodeRemoverImpl;
import com.catalog.infrastructure.adapter.category.impl.CategoryNodeRetrieverImpl;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.factory.CategoryComponentFactory;
import com.catalog.infrastructure.repository.jpa.adapter.*;
import com.catalog.infrastructure.repository.jpa.impl.CategoryNodeRepositoryImpl;
import com.catalog.infrastructure.repository.jpa.impl.CategoryQueryRepositoryImpl;
import com.catalog.infrastructure.view.CategoryChildrenView;
import com.catalog.infrastructure.view.CategoryNodeView;
import com.catalog.infrastructure.view.CategoryView;
import com.nestedset.app.config.JpaNestedSetRepositoryConfiguration;
import com.nestedset.app.service.TreeBuilder;
import com.nestedset.app.service.TreeBuilderImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.support.DefaultJpaContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class)
@ContextConfiguration(classes = CategoryQueryRepositoryIntegrationTest.TestConfig.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:categorydb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class CategoryQueryRepositoryIntegrationTest {

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.catalog.infrastructure.entity")
    static class TestConfig {
        @Bean
        EntityManager entityManager(jakarta.persistence.EntityManagerFactory entityManagerFactory) {
            return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        }

        @Bean
        JpaContext jpaContext(EntityManager entityManager) {
            return new DefaultJpaContext(java.util.Set.of(entityManager));
        }

        @Bean
        CategoryComponentFactory categoryComponentFactory() {
            return new CategoryComponentFactory();
        }

        @Bean
        public JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> categoryNestedSetConfig(JpaContext jpaContext) {
            return new JpaNestedSetRepositoryConfiguration<>(jpaContext, CategoryEntity.class);
        }

        @Bean
        public TreeBuilder<CategoryEntity, Long> categoryTreeBuilder(CategoryComponentFactory categoryComponentFactory) {
            return new TreeBuilderImpl<>(categoryComponentFactory);
        }

        @Bean
        CategoryJpaRepo categoryJpaRepo(EntityManager entityManager) {
            return new JpaRepositoryFactory(entityManager).getRepository(CategoryJpaRepo.class);
        }

        @Bean
        public CategoryJpaInsertingDelegate categoryJpaInsertingDelegate(
                JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config
        ) {
            return new CategoryJpaInsertingDelegateImpl(config);
        }

        @Bean
        public CategoryJpaRemovingDelegate categoryJpaRemovingDelegate(
                JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config
        ) {
            return new CategoryJpaRemovingDelegateImpl(config);
        }

        @Bean
        public CategoryJpaRetrievingDelegate categoryJpaRetrievingDelegate(
                JpaNestedSetRepositoryConfiguration<CategoryEntity, Long> config
        ) {
            return new CategoryJpaRetrievingDelegateImpl(config);
        }

        @Bean
        public CategoryNodeInserter categoryNodeInserter(CategoryJpaInsertingDelegate insertingDelegate) {
            return new CategoryNodeInserterImpl(insertingDelegate);
        }

        @Bean
        public CategoryNodeRemover categoryNodeRemover(CategoryJpaRemovingDelegate removingDelegate) {
            return new CategoryNodeRemoverImpl(removingDelegate);
        }

        @Bean
        public CategoryNodeRetriever categoryNodeRetriever(CategoryJpaRetrievingDelegate retrievingDelegate) {
            return new CategoryNodeRetrieverImpl(retrievingDelegate);
        }

        @Bean
        public CategoryNestedSetNodeRepository categoryNestedSetNodeRepository(
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
            return new CategoryNodeRepositoryImpl(categoryJpaRepo,nestedSetNodeRepository);
        }

        @Bean
        CategoryQueryRepository categoryTreeQueryRepository(
                CategoryJpaRepo categoryJpaRepo,
                CategoryNodeRepository categoryNodeRepository
        ) {
            return new CategoryQueryRepositoryImpl(categoryJpaRepo, categoryNodeRepository);
        }
    }

    @Autowired
    private CategoryNodeRepository categoryNodeRepository;

    @Autowired
    private CategoryQueryRepository categoryQueryRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        CategoryEntity electronics = category("cat-1", "Electronics");
        CategoryEntity smartphones = category("cat-2", "Smartphones");
        CategoryEntity laptops = category("cat-3", "Laptops");

        categoryNodeRepository.insert(electronics, null);
        categoryNodeRepository.insert(smartphones, "cat-1");
        categoryNodeRepository.insert(laptops, "cat-1");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findTree_returnsNestedHierarchy() {
        Optional<CategoryNodeView> tree = categoryQueryRepository.findTree("cat-1");

        assertThat(tree).isPresent();
        assertThat(tree.orElseThrow().children())
                .extracting(CategoryNodeView::id)
                .containsExactlyInAnyOrder("cat-2", "cat-3");
    }

    @Test
    void findChildren_returnsImmediateChildren() {
        Optional<CategoryChildrenView> children = categoryQueryRepository.findChildren("cat-1");

        assertThat(children).isPresent();
        assertThat(children.orElseThrow().parentId()).isEqualTo("cat-1");
        assertThat(children.orElseThrow().children())
                .extracting(CategoryView::id)
                .containsExactlyInAnyOrder("cat-2", "cat-3");
    }

    @Test
    void findParent_returnsParentWithGrandparentId() {
        CategoryEntity accessories = category("cat-4", "Accessories");
        categoryNodeRepository.insert(accessories, "cat-2");
        entityManager.flush();
        entityManager.clear();

        Optional<CategoryView> parent = categoryQueryRepository.findParent("cat-4");

        assertThat(parent).isPresent();
        assertThat(parent.orElseThrow().id()).isEqualTo("cat-2");
        assertThat(parent.orElseThrow().parentId()).isEqualTo("cat-1");
    }

    private CategoryEntity category(String uuid, String name) {
        CategoryEntity entity = new CategoryEntity();
        entity.setUuid(uuid);
        entity.setName(name);
        entity.setActive(true);
        entity.setListingAllowed(true);
        entity.setReviewRequired(false);
        entity.setC2cAllowed(true);
        return entity;
    }
}
