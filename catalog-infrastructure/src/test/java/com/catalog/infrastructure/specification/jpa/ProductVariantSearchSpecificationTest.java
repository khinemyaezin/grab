package com.catalog.infrastructure.specification.jpa;

import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.entity.entity.ProductVariationEntity;
import com.catalog.infrastructure.repository.jpa.config.ProductRepositoryTestConfig;
import com.catalog.infrastructure.view.ProductVariantView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductVariantSearchSpecificationTest extends ProductRepositoryTestConfig {

    private static final String MERCHANT_ID = "merchant-1";

    @Autowired
    private EntityManager entityManager;

    private ProductVariantSearchSpecification specification;

    @BeforeEach
    void setUp() {
        specification = new ProductVariantSearchSpecification(entityManager);

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
    void search_withMerchantOnly_returnAllVariantsForMerchant() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .build();

        Page<ProductVariantView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(5, page.getTotalElements());
        assertEquals(5, page.getContent().size());
    }

    @Test
    void search_withQueryMatchingProductName_returnMatchedVariants() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .query("cotton")
                .build();

        Page<ProductVariantView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(3, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(v -> "Premium Cotton T-Shirt".equals(v.productName())));
    }

    @Test
    void search_withQueryMatchingSku_returnMatchedVariants() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .query("JNS")
                .build();

        Page<ProductVariantView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("JNS-BLACK-M", page.getContent().getFirst().sku());
    }

    @Test
    void search_withVariantStatus_returnMatchedVariants() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .variantStatus("DELETED")
                .build();

        Page<ProductVariantView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("TSH-RED-L", page.getContent().getFirst().sku());
    }

    @Test
    void search_withCategoryId_returnVariantsUnderGivenCategoryId() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(MERCHANT_ID)
                .categoryId("cat-2")
                .build();

        Page<ProductVariantView> page = specification.search(criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Slim Fit Jeans", page.getContent().getFirst().productName());
    }

    private void persistProduct(String name, String categoryId, VariantData... variants) {
        ProductEntity product = new ProductEntity();
        product.setUuid(UUID.randomUUID().toString());
        product.setName(name);
        product.setCategoryId(categoryId);
        product.setMerchantId(MERCHANT_ID);
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
