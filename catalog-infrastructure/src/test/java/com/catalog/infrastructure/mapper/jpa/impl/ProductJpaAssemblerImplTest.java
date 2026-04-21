package com.catalog.infrastructure.mapper.jpa.impl;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.entity.entity.MediaEntity;
import com.catalog.infrastructure.entity.entity.ProductDescriptionEntity;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.entity.entity.ProductVariationEntity;
import com.catalog.infrastructure.mapper.jpa.ProductEntityMapper;
import com.catalog.infrastructure.mapper.jpa.ProductMapper;
import com.catalog.infrastructure.mapper.jpa.ProductVariationMapper;
import com.catalog.infrastructure.mapper.jpa.ProductVariantEntityMapper;
import com.catalog.infrastructure.mapper.jpa.ProductVariantMapper;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductJpaAssemblerImplTest {

    private ProductJpaAssemblerImpl assembler;

    @BeforeEach
    void setUp() {
        IdGenerator idGenerator = new UuidGenerator();
        IdMapper idMapper = new IdMapper(idGenerator);

        ProductEntityMapper productEntityMapper = Mappers.getMapper(ProductEntityMapper.class);
        ProductVariantEntityMapper productVariantEntityMapper = Mappers.getMapper(ProductVariantEntityMapper.class);
        ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);
        ProductVariantMapper productVariantMapper = Mappers.getMapper(ProductVariantMapper.class);
        ProductVariationMapper productVariationMapper = Mappers.getMapper(ProductVariationMapper.class);

        inject(productEntityMapper, "idMapper", idMapper);
        inject(productVariantEntityMapper, "idMapper", idMapper);
        inject(productMapper, "idGenerator", idGenerator);
        inject(productVariantMapper, "idGenerator", idGenerator);
        inject(productVariationMapper, "idGenerator", idGenerator);

        assembler = new ProductJpaAssemblerImpl(
                productEntityMapper,
                productVariantEntityMapper,
                productMapper,
                productVariantMapper,
                productVariationMapper
        );
    }

    @Test
    void buildFullEntityGraph_mergesDescriptionsByUuid_andPreservesExistingDatabaseIds() {
        Product product = Product.create(
                id("p1"),
                "T-Shirt",
                id("clothing"),
                null,
                null,
                List.of(
                        new Description(id("1001"), "summary", "Updated Summary", "Updated body"),
                        new Description(id("1002"), "details", "Details", "Detailed body")
                ),
                List.of()
        );

        ProductEntity existingEntity = new ProductEntity();
        existingEntity.setUuid("p1");
        existingEntity.setCategoryId("clothing");

        ProductDescriptionEntity existingSummary = descriptionRow(11L, "1001", "summary", "Old Summary", "Old body");
        ProductDescriptionEntity orphan = descriptionRow(12L, "1999", "orphan", "Orphan", "Unused");
        existingEntity.addProductDescription(existingSummary);
        existingEntity.addProductDescription(orphan);

        ProductEntity result = assembler.buildFullEntityGraph(product, existingEntity);

        assertThat(result).isSameAs(existingEntity);
        assertThat(result.getDescriptions()).hasSize(2);

        ProductDescriptionEntity mergedSummary = findDescription(result, "1001");
        assertThat(mergedSummary).isSameAs(existingSummary);
        assertThat(mergedSummary.getId()).isEqualTo(11L);
        assertThat(mergedSummary.getTitle()).isEqualTo("Updated Summary");
        assertThat(mergedSummary.getDescription()).isEqualTo("Updated body");
        assertThat(mergedSummary.getProduct()).isSameAs(result);

        ProductDescriptionEntity addedDetails = findDescription(result, "1002");
        assertThat(addedDetails.getId()).isNull();
        assertThat(addedDetails.getName()).isEqualTo("details");
        assertThat(addedDetails.getProduct()).isSameAs(result);

        assertThat(result.getDescriptions())
                .extracting(ProductDescriptionEntity::getUuid)
                .containsExactly("1001", "1002");
    }

    @Test
    void buildFullEntityGraph_mergesMediaByPath_andReusesExistingRowInsteadOfCreatingDuplicate() {
        Product product = Product.create(
                id("p1"),
                "Camera",
                id("electronics"),
                null,
                null,
                List.of(),
                List.of(new ProductMedia(id("3001"), "IMAGE", "/images/camera/main.jpg"))
        );

        ProductEntity existingEntity = new ProductEntity();
        existingEntity.setUuid("p1");
        existingEntity.setCategoryId("electronics");

        MediaEntity existingMedia = mediaRow(21L, "3999", "THUMBNAIL", "/images/camera/main.jpg");
        MediaEntity orphan = mediaRow(22L, "3998", "GALLERY", "/images/camera/old.jpg");
        existingEntity.addMedia(existingMedia);
        existingEntity.addMedia(orphan);

        ProductEntity result = assembler.buildFullEntityGraph(product, existingEntity);

        assertThat(result.getMedias()).hasSize(1);
        MediaEntity mergedMedia = result.getMedias().getFirst();
        assertThat(mergedMedia).isSameAs(existingMedia);
        assertThat(mergedMedia.getId()).isEqualTo(21L);
        assertThat(mergedMedia.getUuid()).isEqualTo("3001");
        assertThat(mergedMedia.getType()).isEqualTo("IMAGE");
        assertThat(mergedMedia.getPath()).isEqualTo("/images/camera/main.jpg");
    }

    @Test
    void buildFullEntityGraph_mergesVariantCollections_forAddUpdateAndRemoveCases() {
        Product product = createProduct("p1", "T-Shirt");
        product.addVariant(ProductVariant.create(
                id("v1"),
                "SKU-001-UPDATED",
                List.of(new ProductVariation(id("blue"), id("color")))
        ));
        product.addVariant(ProductVariant.create(
                id("v3"),
                "SKU-003",
                List.of(new ProductVariation(id("green"), id("color")))
        ));

        ProductEntity existingEntity = new ProductEntity();
        existingEntity.setUuid("p1");
        existingEntity.setCategoryId("clothing");

        ProductVariantEntity existingV1 = new ProductVariantEntity();
        existingV1.setId(31L);
        existingV1.setUuid("v1");
        existingV1.setSku("SKU-001");
        existingV1.setStatus("ACTIVE");
        existingEntity.addVariant(existingV1);
        existingV1.addProductVariation(new ProductVariationEntity(
                new ProductVariationEntity.ProductVariationId("old-opt", "old-type", null),
                null
        ));

        ProductVariantEntity orphanV2 = new ProductVariantEntity();
        orphanV2.setId(32L);
        orphanV2.setUuid("v2");
        orphanV2.setSku("SKU-002");
        orphanV2.setStatus("ACTIVE");
        existingEntity.addVariant(orphanV2);

        ProductEntity result = assembler.buildFullEntityGraph(product, existingEntity);

        assertThat(result.getProductVariants())
                .extracting(ProductVariantEntity::getUuid)
                .containsExactlyInAnyOrder("v1", "v3");

        ProductVariantEntity mergedV1 = findVariant(result, "v1");
        assertThat(mergedV1).isSameAs(existingV1);
        assertThat(mergedV1.getId()).isEqualTo(31L);
        assertThat(mergedV1.getSku()).isEqualTo("SKU-001-UPDATED");
        assertThat(mergedV1.getProductVariations()).hasSize(1);
        assertThat(mergedV1.getProductVariations().getFirst().getId().getVariantOptionUuid()).isEqualTo("blue");
        assertThat(mergedV1.getProductVariations().getFirst().getId().getVariantTypeUuid()).isEqualTo("color");
        assertThat(mergedV1.getProductVariations().getFirst().getProductVariant()).isSameAs(mergedV1);

        ProductVariantEntity addedV3 = findVariant(result, "v3");
        assertThat(addedV3.getId()).isNull();
        assertThat(addedV3.getSku()).isEqualTo("SKU-003");
        assertThat(addedV3.getProduct()).isSameAs(result);
        assertThat(addedV3.getProductVariations()).hasSize(1);
    }

    @Test
    void toFullDomainGraph_mapsDescriptionsMediasVariantsAndVariationIds() {
        ProductEntity entity = new ProductEntity();
        entity.setId(1L);
        entity.setUuid("p1");
        entity.setName("Phone");
        entity.setCategoryId("electronics");
        entity.setStatus(ProductStatus.ACTIVE);
        entity.setSlug("phone");
        ProductDescriptionEntity description = descriptionRow(41L, "desc-1", "summary", "Summary", "Body");
        entity.addProductDescription(description);

        MediaEntity media = mediaRow(42L, "media-1", "IMAGE", "/images/phone/main.jpg");
        entity.addMedia(media);

        ProductVariantEntity variant = new ProductVariantEntity();
        variant.setId(43L);
        variant.setUuid("variant-1");
        variant.setSku("SKU-001");
        variant.setStatus("ACTIVE");
        entity.addVariant(variant);

        variant.addProductVariation(new ProductVariationEntity(
                new ProductVariationEntity.ProductVariationId("opt-red", "type-color", null),
                null
        ));

        Product result = assembler.toFullDomainGraph(entity);

        assertThat(result.getId().getValue()).isEqualTo("p1");
        assertThat(result.getName()).isEqualTo("Phone");
        assertThat(result.getCategoryId().getValue()).isEqualTo("electronics");
        assertThat(result.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.getSlug()).isEqualTo("phone");

        assertThat(result.getDescriptions()).hasSize(1);
        assertThat(result.getDescriptions().getFirst().getId().getValue()).isEqualTo("41");
        assertThat(result.getDescriptions().getFirst().getTitle()).isEqualTo("Summary");

        assertThat(result.getMedias()).hasSize(1);
        assertThat(result.getMedias().getFirst().getId().getValue()).isEqualTo("42");
        assertThat(result.getMedias().getFirst().getPath()).isEqualTo("/images/phone/main.jpg");

        assertThat(result.getVariants()).hasSize(1);
        ProductVariant mappedVariant = result.getVariants().getFirst();
        assertThat(mappedVariant.getId().getValue()).isEqualTo("variant-1");
        assertThat(mappedVariant.getSku()).isEqualTo("SKU-001");
        assertThat(mappedVariant.getVariations()).hasSize(1);
        ProductVariation variation = mappedVariant.getVariations().iterator().next();
        assertThat(variation.getOptionId().getValue()).isEqualTo("opt-red");
        assertThat(variation.getTypeId().getValue()).isEqualTo("type-color");
    }

    private Product createProduct(String productId, String name) {
        return Product.create(id(productId), name, id("clothing"));
    }

    private ProductDescriptionEntity descriptionRow(Long id, String uuid, String name, String title, String description) {
        ProductDescriptionEntity entity = new ProductDescriptionEntity();
        entity.setId(id);
        entity.setUuid(uuid);
        entity.setName(name);
        entity.setTitle(title);
        entity.setDescription(description);
        return entity;
    }

    private MediaEntity mediaRow(Long id, String uuid, String type, String path) {
        MediaEntity entity = new MediaEntity();
        entity.setId(id);
        entity.setUuid(uuid);
        entity.setType(type);
        entity.setPath(path);
        return entity;
    }

    private ProductDescriptionEntity findDescription(ProductEntity entity, String uuid) {
        return entity.getDescriptions().stream()
                .filter(description -> uuid.equals(description.getUuid()))
                .findFirst()
                .orElseThrow();
    }

    private ProductVariantEntity findVariant(ProductEntity entity, String uuid) {
        return entity.getProductVariants().stream()
                .filter(variant -> uuid.equals(variant.getUuid()))
                .findFirst()
                .orElseThrow();
    }

    private static Id id(String value) {
        return new CommonId(value);
    }

    private static void inject(Object target, String fieldName, Object value) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to set field '" + fieldName + "'", e);
            }
        }
        throw new IllegalArgumentException("Field '" + fieldName + "' not found on " + target.getClass());
    }
}
