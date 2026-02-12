package com.catalog.infrastructure.specification.jpa;

import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.entity.entity.ProductVariationEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = ProductSummaryJpqlQueryTest.TestConfig.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class ProductSummaryJpqlQueryTest {

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.catalog.infrastructure.entity")
    static class TestConfig {
    }

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        persistProduct("Premium Cotton T-Shirt", "cat-1",
                variant("TSH-RED-XS", "ACTIVE",
                        variation("opt-red-1", "type-color-1", "Red", "Color"),
                        variation("opt-xs-1", "type-size-1", "XS", "Size")),
                variant("TSH-BLUE-L", "ACTIVE",
                        variation("opt-blue-1", "type-color-2", "Blue", "Color"),
                        variation("opt-l-1", "type-size-2", "L", "Size")),
                variant("TSH-RED-L", "DELETED",
                        variation("opt-red-2", "type-color-3", "Red", "Color"),
                        variation("opt-l-2", "type-size-3", "L", "Size"))
        );

        persistProduct("Slim Fit Jeans", "cat-2",
                variant("JNS-BLACK-M", "ACTIVE",
                        variation("opt-black-1", "type-color-4", "Black", "Color"),
                        variation("opt-m-1", "type-size-4", "M", "Size"))
        );

        persistProduct("Running Shoes", "cat-3",
                variant("SHO-WHITE-42", "ACTIVE",
                        variation("opt-white-1", "type-color-5", "White", "Color"),
                        variation("opt-42-1", "type-shoesize-1", "42", "Shoe Size"))
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void search_withNoCriteria_returnAllProducts() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder().build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(3, page.getTotalElements());
        assertEquals(3, page.getContent().size());
    }

    @Test
    void search_withProductNameCaseInsensitivePartialMatch_returnMatchedProduct() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .productName("cotton")
                .build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Premium Cotton T-Shirt", page.getContent().getFirst().getName());
    }

    @Test
    void search_withMultipleMatchesProductName_returnMatchedProducts() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .productName("s")
                .build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(3, page.getTotalElements());
    }

    @Test
    void search_withSku_returnMatchedProducts() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .sku("JNS")
                .build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Slim Fit Jeans", page.getContent().getFirst().getName());
    }

    @Test
    void search_withSkuCaseInsensitive_returnMatchedProducts() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .sku("tsh-red")
                .build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Premium Cotton T-Shirt", page.getContent().getFirst().getName());
    }

    @Test
    void search_withStatus_returnMatchedProducts() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .variantStatus("DELETED")
                .build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Premium Cotton T-Shirt", page.getContent().getFirst().getName());
    }

    @Test
    void search_withActiveStatus_returnActiveProducts() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .variantStatus("ACTIVE")
                .build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(3, page.getTotalElements());
    }

    @Test
    void search_withVariationOptionValue_returnMatchedProducts() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .variations(List.of("Red"))
                .build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Premium Cotton T-Shirt", page.getContent().getFirst().getName());
    }

    @Test
    void search_withMultipleVariationValues_returnMatchedProducts() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .variations(List.of("Black", "M"))
                .build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Slim Fit Jeans", page.getContent().getFirst().getName());
    }

    @Test
    void search_withPageNumberAndPageSize_returnCorrectPageWithLimitedSize() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder().build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 2));

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void search_withSecondPageNumber_returnSecondPageNumber() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder().build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(1, 2));

        assertEquals(3, page.getTotalElements());
        assertEquals(1, page.getContent().size());
    }

    @Test
    void search_withCategoryId_returnProductsUnderGivenCategoryId() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .categoryId("cat-2")
                .build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Slim Fit Jeans", page.getContent().getFirst().getName());
    }

    @Test
    void search_withNoFeatured_returnProductsUnderGivenCategoryId() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .feature(false)
                .build();

        Page<ProductEntity> page = ProductSummaryJpqlQuery.search(
                entityManager, criteria, PageRequest.of(0, 10));

        assertEquals(3, page.getTotalElements());
    }


    private void persistProduct(String name, String categoryId, VariantData... variants) {
        ProductEntity product = new ProductEntity();
        product.setUuid(UUID.randomUUID().toString());
        product.setName(name);
        product.setCategoryId(categoryId);
        entityManager.persist(product);

        for (VariantData v : variants) {
            ProductVariantEntity variantEntity = new ProductVariantEntity();
            variantEntity.setUuid(UUID.randomUUID().toString());
            variantEntity.setSku(v.sku);
            variantEntity.setStatus(v.status);
            product.addVariant(variantEntity);
            entityManager.persist(variantEntity);

            for (VariationData vd : v.variations) {
                ProductVariationEntity variationEntity = new ProductVariationEntity();
                variationEntity.setId(new ProductVariationEntity.ProductVariationId(
                        vd.optionId, vd.typeId, null));
                variationEntity.setVariantOptionValue(vd.optionValue);
                variationEntity.setVariantTypeValue(vd.typeValue);
                variantEntity.addProductVariation(variationEntity);
                entityManager.persist(variationEntity);
            }
        }
    }

    private static VariantData variant(String sku, String status, VariationData... variations) {
        return new VariantData(sku, status, List.of(variations));
    }

    private static VariationData variation(String optionId, String typeId,
                                           String optionValue, String typeValue) {
        return new VariationData(optionId, typeId, optionValue, typeValue);
    }

    private record VariantData(String sku, String status, List<VariationData> variations) {}

    private record VariationData(String optionId, String typeId,
                                 String optionValue, String typeValue) {}

}
