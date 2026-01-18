package com.product.infrastructure.service.impl;

import com.product.domain.aggregate.product.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import com.product.infrastructure.entity.product.entity.VariantOptionEntity;
import com.product.infrastructure.entity.product.entity.VariantTypeEntity;
import com.product.infrastructure.entity.product.factory.ProductVariantOptionEntityFactory;
import com.product.infrastructure.mapper.product.ProductVariationMapper;
import com.product.infrastructure.repository.jpa.VariantOptionJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductVariantOptionServiceImplTest extends ProductTestData{

    @Mock
    private ProductVariantOptionEntityFactory productVariantOptionEntityFactory;

    @Mock
    private ProductVariationMapper productVariationMapper;

    @Mock
    private VariantOptionJpaRepository variantOptionJpaRepository;

    @InjectMocks
    private ProductVariantOptionServiceImpl service;

    @Test
    void updateVariations_withNewVariations_shouldExistNewVariations() {
        VariantOptionEntity redOptionEntity = new VariantOptionEntity();
        redOptionEntity.setUuid("opt-red");
        VariantOptionEntity smallOptionEntity = new VariantOptionEntity();
        smallOptionEntity.setUuid("opt-s");

        ProductVariationEntity redEntity = new ProductVariationEntity();
        redEntity.setId(1L);
        redEntity.setVariantOption(redOptionEntity);
        ProductVariationEntity smallEntity = new ProductVariationEntity();
        smallEntity.setId(2L);
        smallEntity.setVariantOption(smallOptionEntity);

        ProductVariation redDomain = new ProductVariation("Red", id("opt-red"), "Color",id("color"));
        ProductVariation smallDomain = new ProductVariation("Small", id("opt-s"), "Size", id("size"));
        ProductVariation blueDomain = new ProductVariation("Blue", id("opt-blue"), "Color",id("color"));
        ProductVariation mediumDomain = new ProductVariation("Medium", id("opt-medium"), "Size", id("size"));

        when(productVariationMapper.toDomain(redEntity)).thenReturn(redDomain);
        when(productVariationMapper.toDomain(smallEntity)).thenReturn(smallDomain);

        // Existing Variant : Red - Small
        ProductVariantEntity variantEntity = new ProductVariantEntity();
        variantEntity.addProductVariantOption(redEntity);
        variantEntity.addProductVariantOption(smallEntity);

        VariantOptionEntity blueOption = new VariantOptionEntity();
        blueOption.setUuid(blueDomain.getOptionId().getValue());

        VariantTypeEntity colorType = new VariantTypeEntity();
        blueOption.setVariantType(colorType);

        when(variantOptionJpaRepository.findByUuid(blueDomain.getOptionId().getValue()))
                .thenReturn(Optional.of(blueOption));

        VariantOptionEntity mediumOption = new VariantOptionEntity();
        mediumOption.setUuid(mediumDomain.getOptionId().getValue());

        VariantTypeEntity sizeType = new VariantTypeEntity();
        mediumOption.setVariantType(sizeType);

        when(variantOptionJpaRepository.findByUuid(mediumDomain.getOptionId().getValue()))
                .thenReturn(Optional.of(mediumOption));

        ProductVariationEntity blueVariationEntity = new ProductVariationEntity();
        when(productVariantOptionEntityFactory.create(eq(blueDomain), any(), any()))
                .thenAnswer(invocation -> blueVariationEntity);
        ProductVariationEntity mediumVariationEntity = new ProductVariationEntity();
        when(productVariantOptionEntityFactory.create(eq(mediumDomain), any(), any()))
                .thenAnswer(invocation -> mediumVariationEntity);

        // Replace Red-Small to Blue-Medium
        Set<ProductVariation> updates = new LinkedHashSet<>();
        updates.add(blueDomain);
        updates.add(mediumDomain);

        service.updateVariations(variantEntity, updates);

        assertThat(variantEntity.getProductVariations()).hasSize(2);
        assertThat(variantEntity.getProductVariations()).contains(blueVariationEntity, mediumVariationEntity);

        verify(variantOptionJpaRepository, times(1)).findByUuid(blueDomain.getOptionId().getValue());
        verify(productVariantOptionEntityFactory, times(2)).create(any(ProductVariation.class), any(), any());
        verify(productVariationMapper, times(2)).toDomain(any(ProductVariationEntity.class));
    }
}
