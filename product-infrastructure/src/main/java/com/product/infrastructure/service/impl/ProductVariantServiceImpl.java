package com.product.infrastructure.service.impl;

import com.product.domain.aggregate.product.ProductVariant;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.factory.ProductVariantEntityFactory;
import com.product.infrastructure.mapper.product.ProductVariantEntityMapper;
import com.product.infrastructure.repository.jpa.ProductVariantJpaRepository;
import com.product.infrastructure.service.ProductVariantOptionService;
import com.product.infrastructure.service.ProductVariantService;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {
    private final ProductVariantEntityFactory productVariantEntityFactory;
    private final ProductVariantOptionService productVariantOptionService;
    private final ProductVariantJpaRepository productVariantJpaRepository;
    private final ProductVariantEntityMapper productVariantEntityMapper;

    @Override
    public Optional<ProductVariantEntity> find(String uuid) {
        return this.productVariantJpaRepository.findByUuid(uuid);
    }

    @Override
    public void updateVariants(ProductEntity productEntity, List<ProductVariant> productVariants) {
        Map<String, ProductVariant> domainVariantMap = productVariants.stream()
                .collect(Collectors.toMap(
                        productVariant -> productVariant.getId().getValue(),
                        Function.identity(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        List<ProductVariantEntity> removedProductVariantEntityList = getRemovedVariants(productEntity.getProductVariants(), domainVariantMap);
        productEntity.getProductVariants().removeAll(removedProductVariantEntityList);

        List<ProductVariantEntity> newProductVariantEntityList = getNewVariants(domainVariantMap);
        for(ProductVariantEntity entity:newProductVariantEntityList) {
            productEntity.addVariant(entity);
        }
    }

    protected List<ProductVariantEntity> getRemovedVariants(List<ProductVariantEntity> productVariantEntityList, Map<String, ProductVariant> domainVariantMap) {
        List<ProductVariantEntity> removedProductVariantEntityList = new ArrayList<>();
        for (ProductVariantEntity variantEntity : productVariantEntityList) {
            ProductVariant matchVariant = domainVariantMap.get(variantEntity.getUuid());
            if (Objects.nonNull(matchVariant)) {
                productVariantEntityMapper.map(matchVariant, variantEntity);
                productVariantOptionService.updateVariations(variantEntity, matchVariant.getVariations());
                domainVariantMap.remove(variantEntity.getUuid());
            } else {
                removedProductVariantEntityList.add(variantEntity);
            }
        }
        return removedProductVariantEntityList;
    }

    protected List<ProductVariantEntity> getNewVariants(Map<String, ProductVariant> remainingVariantsMap) {
        List<ProductVariantEntity> newProductVariantEntityList = new ArrayList<>();
        for (ProductVariant productVariant : remainingVariantsMap.values()) {
            ProductVariantEntity productVariantEntity = this.productVariantEntityFactory.create(productVariant);
            productVariantOptionService.updateVariations(productVariantEntity, productVariant.getVariations());
            newProductVariantEntityList.add(productVariantEntity);
        }
        return newProductVariantEntityList;
    }
}
