package com.product.infrastructure.mapper.jpa.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.valueobject.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import com.product.infrastructure.mapper.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductJpaAssemblerImplTest {

    private ProductEntityMapper productEntityMapper;
    private ProductVariantEntityMapper variantEntityMapper;
    private ProductMapper productMapper;
    private ProductVariantMapper productVariantMapper;
    private ProductVariationMapper productVariationMapper;
    private ProductJpaAssemblerImpl assembler;

    @BeforeEach
    void setUp() {
        productEntityMapper = mock(ProductEntityMapper.class);
        variantEntityMapper = mock(ProductVariantEntityMapper.class);
        productMapper = mock(ProductMapper.class);
        productVariantMapper = mock(ProductVariantMapper.class);
        productVariationMapper = mock(ProductVariationMapper.class);
        assembler = new ProductJpaAssemblerImpl(
                productEntityMapper, variantEntityMapper,
                productMapper, productVariantMapper, productVariationMapper
        );
    }

    @Nested
    class ToFullEntityGraph_Create {

        @Test
        void createsNewEntityWhenExistingIsNull() {
            Product product = createProduct("p1", "T-Shirt");

            stubVariantEntityMapper();

            ProductEntity result = assembler.toFullEntityGraph(product, null);

            assertNotNull(result);
            verify(productEntityMapper).toEntity(eq(product), any(ProductEntity.class));
        }

        @Test
        void addsVariantsToNewEntity() {
            Product product = createProductWithVariants();

            stubVariantEntityMapper();

            ProductEntity result = assembler.toFullEntityGraph(product, null);

            assertEquals(product.getVariants().size(), result.getProductVariants().size());
        }

        @Test
        void addsVariationsToEachVariant() {
            Product product = createProductWithVariants();

            stubVariantEntityMapper();

            ProductEntity result = assembler.toFullEntityGraph(product, null);

            for (ProductVariantEntity variantEntity : result.getProductVariants()) {
                assertFalse(variantEntity.getProductVariations().isEmpty());
            }
        }

        @Test
        void mapsVariationIdsCorrectly() {
            Product product = createProduct("p1", "T-Shirt");
            ProductVariant variant = ProductVariant.create(
                    id("v1"), "SKU-001",
                    List.of(new ProductVariation("Red", id("opt-red"), "Color", id("type-color")))
            );
            product.addVariant(variant);

            stubVariantEntityMapper();

            ProductEntity result = assembler.toFullEntityGraph(product, null);

            ProductVariantEntity variantEntity = result.getProductVariants().get(0);
            ProductVariationEntity variationEntity = variantEntity.getProductVariations().get(0);

            assertEquals("opt-red", variationEntity.getId().getVariantOptionUuid());
            assertEquals("type-color", variationEntity.getId().getVariantTypeUuid());
            assertEquals("Red", variationEntity.getVariantOptionValue());
            assertEquals("Color", variationEntity.getVariantTypeValue());
        }

        @Test
        void setsVariantBackReferenceOnVariations() {
            Product product = createProductWithVariants();

            stubVariantEntityMapper();

            ProductEntity result = assembler.toFullEntityGraph(product, null);

            for (ProductVariantEntity variantEntity : result.getProductVariants()) {
                for (ProductVariationEntity variation : variantEntity.getProductVariations()) {
                    assertSame(variantEntity, variation.getProductVariant());
                }
            }
        }
    }

    @Nested
    class ToFullEntityGraph_Merge {

        @Test
        void mergesIntoExistingEntity() {
            Product product = createProductWithVariants();
            ProductEntity existingEntity = createExistingEntity(product);

            stubVariantEntityMapper();

            ProductEntity result = assembler.toFullEntityGraph(product, existingEntity);

            assertSame(existingEntity, result);
            verify(productEntityMapper).toEntity(eq(product), same(existingEntity));
        }

        @Test
        void updatesExistingVariant() {
            Product product = createProduct("p1", "T-Shirt");
            ProductVariant variant = ProductVariant.create(
                    id("v1"), "SKU-001",
                    List.of(new ProductVariation("Red", id("red"), "Color", id("color")))
            );
            product.addVariant(variant);

            ProductEntity existingEntity = new ProductEntity();
            existingEntity.setUuid("p1");
            ProductVariantEntity existingVariant = new ProductVariantEntity();
            existingVariant.setUuid("v1");
            existingVariant.setSku("SKU-001-OLD");
            existingEntity.addVariant(existingVariant);

            stubVariantEntityMapper();

            assembler.toFullEntityGraph(product, existingEntity);

            verify(variantEntityMapper).map(eq(variant), same(existingVariant));
        }

        @Test
        void removesVariantNotInDomain() {
            Product product = createProduct("p1", "T-Shirt");
            // product has no variants

            ProductEntity existingEntity = new ProductEntity();
            existingEntity.setUuid("p1");
            ProductVariantEntity orphanVariant = new ProductVariantEntity();
            orphanVariant.setUuid("orphan-v1");
            orphanVariant.setSku("SKU-ORPHAN");
            existingEntity.addVariant(orphanVariant);

            assembler.toFullEntityGraph(product, existingEntity);

            assertTrue(existingEntity.getProductVariants().isEmpty());
        }

        @Test
        void addsNewVariantDuringMerge() {
            Product product = createProduct("p1", "T-Shirt");
            ProductVariant newVariant = ProductVariant.create(
                    id("v-new"), "SKU-NEW",
                    List.of(new ProductVariation("Blue", id("blue"), "Color", id("color")))
            );
            product.addVariant(newVariant);

            ProductEntity existingEntity = new ProductEntity();
            existingEntity.setUuid("p1");
            // no existing variants

            stubVariantEntityMapper();

            assembler.toFullEntityGraph(product, existingEntity);

            assertEquals(1, existingEntity.getProductVariants().size());
        }

        @Test
        void replacesVariationsOnExistingVariant() {
            Product product = createProduct("p1", "T-Shirt");
            ProductVariant variant = ProductVariant.create(
                    id("v1"), "SKU-001",
                    List.of(new ProductVariation("Blue", id("blue"), "Color", id("color")))
            );
            product.addVariant(variant);

            ProductEntity existingEntity = new ProductEntity();
            existingEntity.setUuid("p1");
            ProductVariantEntity existingVariant = new ProductVariantEntity();
            existingVariant.setUuid("v1");
            existingVariant.setSku("SKU-001");
            // old variation
            ProductVariationEntity oldVariation = new ProductVariationEntity(
                    new ProductVariationEntity.ProductVariationId("old-opt", "old-type"),
                    null, "OldOption", "OldType"
            );
            existingVariant.addProductVariation(oldVariation);
            existingEntity.addVariant(existingVariant);

            stubVariantEntityMapper();

            assembler.toFullEntityGraph(product, existingEntity);

            List<ProductVariationEntity> variations = existingVariant.getProductVariations();
            assertEquals(1, variations.size());
            assertEquals("blue", variations.get(0).getId().getVariantOptionUuid());
        }

        @Test
        void handlesSimultaneousAddUpdateRemove() {
            // Domain has: v1 (update), v3 (new)
            // Existing has: v1 (keep), v2 (remove)
            Product product = createProduct("p1", "T-Shirt");
            ProductVariant v1 = ProductVariant.create(id("v1"), "SKU-001-UPDATED",
                    List.of(new ProductVariation("Red", id("red"), "Color", id("color"))));
            ProductVariant v3 = ProductVariant.create(id("v3"), "SKU-003",
                    List.of(new ProductVariation("Green", id("green"), "Color", id("color"))));
            product.addVariant(v1);
            product.addVariant(v3);

            ProductEntity existingEntity = new ProductEntity();
            existingEntity.setUuid("p1");

            ProductVariantEntity existingV1 = new ProductVariantEntity();
            existingV1.setUuid("v1");
            existingV1.setSku("SKU-001");
            existingEntity.addVariant(existingV1);

            ProductVariantEntity existingV2 = new ProductVariantEntity();
            existingV2.setUuid("v2");
            existingV2.setSku("SKU-002");
            existingEntity.addVariant(existingV2);

            stubVariantEntityMapper();

            assembler.toFullEntityGraph(product, existingEntity);

            List<String> uuids = existingEntity.getProductVariants().stream()
                    .map(ProductVariantEntity::getUuid)
                    .toList();
            assertEquals(2, uuids.size());
            assertTrue(uuids.contains("v1"));
            assertTrue(uuids.contains("v3"));
            assertFalse(uuids.contains("v2"));
        }
    }

    @Nested
    class ToFullDomainGraph {

        @Test
        void mapsEntityToDomain() {
            ProductEntity entity = new ProductEntity();
            entity.setUuid("p1");
            entity.setName("T-Shirt");
            entity.setCategoryId("cat1");

            Product expectedProduct = createProduct("p1", "T-Shirt");
            when(productMapper.toDomain(eq(entity), anyList())).thenReturn(expectedProduct);

            Product result = assembler.toFullDomainGraph(entity);

            assertSame(expectedProduct, result);
        }

        @Test
        void mapsVariantsAndVariations() {
            ProductEntity entity = new ProductEntity();
            entity.setUuid("p1");
            entity.setName("T-Shirt");
            entity.setCategoryId("cat1");

            ProductVariantEntity variantEntity = new ProductVariantEntity();
            variantEntity.setUuid("v1");
            variantEntity.setSku("SKU-001");
            variantEntity.setStatus("ACTIVE");
            entity.addVariant(variantEntity);

            ProductVariationEntity variationEntity = new ProductVariationEntity(
                    new ProductVariationEntity.ProductVariationId("red", "color"),
                    null, "Red", "Color"
            );
            variantEntity.addProductVariation(variationEntity);

            ProductVariation domainVariation = new ProductVariation("Red", id("red"), "Color", id("color"));
            when(productVariationMapper.toDomain(variationEntity)).thenReturn(domainVariation);

            ProductVariant domainVariant = ProductVariant.create(id("v1"), "SKU-001", List.of(domainVariation));
            when(productVariantMapper.toDomain(eq(variantEntity), anyList())).thenReturn(domainVariant);

            Product expectedProduct = createProduct("p1", "T-Shirt");
            when(productMapper.toDomain(eq(entity), anyList())).thenReturn(expectedProduct);

            assembler.toFullDomainGraph(entity);

            verify(productVariationMapper).toDomain(variationEntity);
            verify(productVariantMapper).toDomain(eq(variantEntity), argThat(variations ->
                    variations.size() == 1 && variations.get(0) == domainVariation));
            verify(productMapper).toDomain(eq(entity), argThat(variants ->
                    variants.size() == 1 && variants.get(0) == domainVariant));
        }

        @Test
        void handlesEntityWithNoVariants() {
            ProductEntity entity = new ProductEntity();
            entity.setUuid("p1");
            entity.setName("T-Shirt");
            entity.setCategoryId("cat1");

            Product expectedProduct = createProduct("p1", "T-Shirt");
            when(productMapper.toDomain(eq(entity), eq(List.of()))).thenReturn(expectedProduct);

            Product result = assembler.toFullDomainGraph(entity);

            assertSame(expectedProduct, result);
            verify(productMapper).toDomain(entity, List.of());
            verifyNoInteractions(productVariantMapper);
            verifyNoInteractions(productVariationMapper);
        }
    }

    private void stubVariantEntityMapper() {
        doAnswer(invocation -> {
            ProductVariant source = invocation.getArgument(0);
            ProductVariantEntity target = invocation.getArgument(1);
            target.setUuid(source.getId().getValue());
            target.setSku(source.getSku());
            target.setStatus(source.getStatus().name());
            return null;
        }).when(variantEntityMapper).map(any(ProductVariant.class), any(ProductVariantEntity.class));
    }

    private Product createProduct(String id, String name) {
        return Product.create(id(id), name, id("clothing"));
    }

    private Product createProductWithVariants() {
        Product product = createProduct("p1", "T-Shirt");

        ProductVariant v1 = ProductVariant.create(id("v1"), "SKU-RED-S",
                List.of(
                        new ProductVariation("Red", id("red"), "Color", id("color")),
                        new ProductVariation("Small", id("small"), "Size", id("size"))
                ));
        ProductVariant v2 = ProductVariant.create(id("v2"), "SKU-BLUE-S",
                List.of(
                        new ProductVariation("Blue", id("blue"), "Color", id("color")),
                        new ProductVariation("Small", id("small"), "Size", id("size"))
                ));
        product.addVariant(v1);
        product.addVariant(v2);
        return product;
    }

    private ProductEntity createExistingEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setUuid(product.getId().getValue());
        entity.setName(product.getName());
        entity.setCategoryId("clothing");

        for (ProductVariant variant : product.getVariants()) {
            ProductVariantEntity variantEntity = new ProductVariantEntity();
            variantEntity.setUuid(variant.getId().getValue());
            variantEntity.setSku(variant.getSku());
            variantEntity.setStatus(variant.getStatus().name());
            entity.addVariant(variantEntity);
        }
        return entity;
    }

    private static Id id(String value) {
        return new Id() {
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Id other)) return false;
                return Objects.equals(value, other.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(value);
            }

            @Override
            public String toString() {
                return value;
            }
        };
    }
}