package com.inventory.adapter.persistence.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import com.inventory.adapter.persistence.mapper.ProductVariantProjectionMapper;
import com.inventory.adapter.persistence.mapper.ProductVariantProjectionMapperImpl;
import com.inventory.adapter.persistence.repository.jpa.ProductVariantViewJpaRepository;
import com.inventory.adapter.persistence.repository.jpa.config.RepositoryTestConfig;
import com.inventory.application.model.write.ProductVariantProjection;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import static org.assertj.core.api.Assertions.assertThat;

@Import({ProductVariantProjectionMapperImpl.class, ProductVariantProjectionRepositoryPostgresTest.MappingConfig.class})
class ProductVariantProjectionRepositoryPostgresTest extends RepositoryTestConfig {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProductVariantProjectionMapper mapper;
    private ProductVariantViewJpaRepository repository;
    private ProductVariantProjectionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
        repository = factory.getRepository(ProductVariantViewJpaRepository.class);
        InventoryPersistenceExecutor executor = new InventoryPersistenceExecutor();
        adapter = new ProductVariantProjectionRepositoryAdapter(repository, mapper, executor);
    }

    @Test
    void save_existingVariant_updatesFactsWithoutDuplicateMembership() {
        Id productId = new CommonId("product-1");
        Id variantId = new CommonId("variant-1");
        ProductVariantProjection initial = new ProductVariantProjection(
                productId, variantId, "SKU1", "Product", true, "ACTIVE"
        );
        adapter.save(initial);
        entityManager.flush();
        entityManager.clear();

        assertThat(adapter.findByVariant(variantId)).contains(initial);
        ProductVariantProjection deleted = new ProductVariantProjection(
                productId, variantId, "SKU2", "Renamed", false, "DELETED"
        );
        adapter.save(deleted);
        entityManager.flush();
        entityManager.clear();

        assertThat(adapter.findByVariant(variantId)).contains(deleted);
        assertThat(adapter.findByProduct(productId)).containsExactly(deleted);
        assertThat(repository.findAllByProductUuid("product-1")).hasSize(1);
    }

    @TestConfiguration
    static class MappingConfig {
        @Bean
        IdMapper idMapper() {
            UuidGenerator idGenerator = new UuidGenerator();
            return new IdMapper(idGenerator);
        }
    }
}
