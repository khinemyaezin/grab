package com.product.infrastructure.service.impl;

import com.product.domain.aggregate.product.*;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.factory.ProductVariantEntityFactory;
import com.product.infrastructure.mapper.product.ProductVariantEntityMapper;
import com.product.infrastructure.repository.jpa.ProductVariantJpaRepository;
import com.product.infrastructure.service.ProductVariantOptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductVariantServiceTest extends ProductTest {

    @Mock
    private ProductVariantEntityFactory productVariantEntityFactory;

    @Mock
    private ProductVariantOptionService productVariantOptionService;

    @Mock
    private ProductVariantJpaRepository productVariantJpaRepository;

    @Mock
    private ProductVariantEntityMapper productVariantEntityMapper;

    @InjectMocks
    private ProductVariantServiceImpl productVariantService;

    @BeforeEach
    public void setUp() {
        doAnswer(invocation -> {
            ProductVariant variant = invocation.getArgument(0);
            ProductVariantEntity entity = invocation.getArgument(1);
            entity.setSku(variant.getSku());
            entity.setUuid(variant.getId().getValue());
            return null; // void method returns null
        }).when(productVariantEntityMapper).map(any(ProductVariant.class), any(ProductVariantEntity.class));
    }


    /**
     * [
     * 		TSH-BLUE-XS : BlueXS
     * 		TSH-RED-S : RedS
     * 		TSH-RED-XS : RedXS
     * 		UPDATED-TSH-BLUE-S : BlueS
     * 	]
     */
    @Test
    void updateVariants_shouldUpdateAllVariants() {
        Product product = super.createProduct();
        ProductEntity productEntity = createProductEntity(product);

        ProductVariant variant = product.getVariants().get(0);
        int originalVariantCount = productEntity.getProductVariants().size();
        String updatedSku = "UPDATED-" + variant.getSku();

        ProductVariant modifiedVariant = new ProductVariant(
            variant.getId(),
            variant.getProductId(), updatedSku,
            new ArrayList<>(variant.getVariations())
        );
        product.removeVariant(variant.getId());
        product.addVariant(modifiedVariant);

        productVariantService.updateVariants(productEntity, product.getVariants());

        assertEquals(product.getVariants().size(), productEntity.getProductVariants().size());
        
        Optional<ProductVariantEntity> updatedEntity = productEntity.getProductVariants().stream()
            .filter(variantEntity -> variantEntity.getUuid().equals(modifiedVariant.getId().getValue()))
            .findFirst();
        
        assertTrue(updatedEntity.isPresent(), "Updated variant should be present in entity");
        assertEquals(updatedSku, updatedEntity.get().getSku(), "Entity should reflect the updated SKU");
        
        verify(productVariantEntityMapper, times(originalVariantCount)).map(any(ProductVariant.class), any(ProductVariantEntity.class));
        verify(productVariantOptionService, times(product.getVariants().size())).updateVariations(any(ProductVariantEntity.class), any());
    }

    /**
     * [
     * 		TSH-BLUE-XS : BlueXS
     * 		TSH-RED-S : RedS
     * 		TSH-RED-XS : RedXS
     * 		TSH-BLUE-S : BlueS
     * 	]
     */
    @Test
    public void updateVariants_shouldCreateAllVariants() {
        when(productVariantEntityFactory.create(any(ProductVariant.class)))
            .thenAnswer(invocation -> {
                ProductVariant variant = invocation.getArgument(0);
                ProductVariantEntity newVariant = new ProductVariantEntity();
                productVariantEntityMapper.map(variant, newVariant);
                return newVariant;
            });

        ProductEntity productEntity = new ProductEntity();
        Product product = super.createProduct();

        productVariantService.updateVariants(productEntity, product.getVariants());

        // Verify all variants match with entity variants
        assertEquals(product.getVariants().size(), productEntity.getProductVariants().size(), 
            "Entity should have the same number of variants as domain model");
        
        for (ProductVariant domainVariant : product.getVariants()) {
            Optional<ProductVariantEntity> matchingEntity = productEntity.getProductVariants().stream()
                .filter(entityVariant -> entityVariant.getUuid().equals(domainVariant.getId().getValue()))
                .findFirst();
            
            assertTrue(matchingEntity.isPresent(), 
                "Entity should contain variant with UUID: " + domainVariant.getId().getValue());
            assertEquals(domainVariant.getSku(), matchingEntity.get().getSku(), 
                "Entity variant SKU should match domain variant SKU");
        }
        
        verify(productVariantEntityFactory, times(product.getVariants().size())).create(any(ProductVariant.class));
        verify(productVariantOptionService, times(product.getVariants().size())).updateVariations(any(ProductVariantEntity.class), any());
    }

    /**
     * [
     * 		TSH-BLUE-XS : BlueXS
     * 		TSH-RED-S : RedS
     * 		TSH-RED-XS : RedXS
     * 		NEW-TSH-BLUE-S : BlueS
     * 	]
     */
    @Test
    public void updateVariants_newVariants_shouldCreateVariants() {
        Product product = super.createProduct();
        ProductEntity productEntity = createProductEntity(product);
        ProductVariant newProductVariant = product.getVariants().getFirst();
        ProductVariantEntity removedProductVariantEntity = productEntity.getProductVariants().stream()
                .filter(v->v.getUuid().equals(newProductVariant.getId().getValue()))
                .findFirst().get();
        productEntity.getProductVariants().remove(removedProductVariantEntity);

        when(productVariantEntityFactory.create(any(ProductVariant.class)))
                .thenAnswer(invocation -> {
                    ProductVariant variant = invocation.getArgument(0);
                    assertEquals(newProductVariant.getId(),variant.getId());
                    return new ProductVariantEntity();
                });
        productVariantService.updateVariants(productEntity, product.getVariants());
    }
} 