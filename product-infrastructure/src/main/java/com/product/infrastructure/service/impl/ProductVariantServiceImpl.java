package com.product.infrastructure.service.impl;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.factory.ProductVariantEntityFactory;
import com.product.infrastructure.mapper.product.ProductVariantEntityMapper;
import com.product.infrastructure.repository.jpa.ProductVariantJpaRepository;
import com.product.infrastructure.service.ProductVariantOptionService;
import com.product.infrastructure.service.ProductVariantService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
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
        Map<Id, ProductVariant> domainVariantMap = productVariants.stream()
                .collect(Collectors.toMap(
                        Entity::getId,
                        Function.identity(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        mergeAndRemoveVariants(productEntity, domainVariantMap);
        addNewVariants(productEntity, domainVariantMap);
    }

    private void mergeAndRemoveVariants(ProductEntity productEntity, Map<Id, ProductVariant> inputVariantsMap) {
        for (ProductVariantEntity existingVariant : productEntity.getProductVariants()) {
            ProductVariant inputVariant = inputVariantsMap.get(existingVariant.getUuid());
            if (Objects.nonNull(inputVariant)) {
                productVariantEntityMapper.map(inputVariant, existingVariant);
                productVariantOptionService.updateVariations(existingVariant, inputVariant.getVariations());
                inputVariantsMap.remove(existingVariant.getUuid());
            } else {
                productEntity.removeVariant(existingVariant);
            }
        }
    }

    private void addNewVariants(ProductEntity productEntity, Map<Id, ProductVariant> remainingVariantsMap) {
        for (ProductVariant inputVariant : remainingVariantsMap.values()) {
            ProductVariantEntity productVariantEntity = this.productVariantEntityFactory.create(inputVariant);
            productVariantOptionService.updateVariations(productVariantEntity, inputVariant.getVariations());
            productEntity.addVariant(productVariantEntity);
        }
    }
}
