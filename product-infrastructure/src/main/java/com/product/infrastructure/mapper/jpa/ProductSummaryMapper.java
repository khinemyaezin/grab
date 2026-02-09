package com.product.infrastructure.mapper.jpa;

import com.grab.framework.mapper.CommonMapper;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import com.product.infrastructure.mapper.CentralMapperConfig;
import com.product.infrastructure.specification.jpa.ProductSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(config = CentralMapperConfig.class,uses = {CommonMapper.class})
public interface ProductSummaryMapper {

    @Mapping(source = "uuid", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "productVariants", target = "variants", qualifiedByName = "VariantSummary")
    ProductSummary toProductSummary(ProductEntity productEntity);

    @Named("VariantSummary")
    default ProductSummary.VariantSummary toVariantSummary(List<ProductVariantEntity> variants){
        boolean available = variants.stream()
                .anyMatch(v -> "ACTIVE".equals(v.getStatus()));

        Map<String, List<String>> options = variants.stream()
                .flatMap(v -> v.getProductVariations().stream())
                .collect(Collectors.groupingBy(
                        v -> v.getId().getVariantTypeUuid(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                v-> v.getId().getVariantOptionUuid(),
                                Collectors.collectingAndThen(
                                        Collectors.toCollection(LinkedHashSet::new),
                                        LinkedHashSet::stream
                                )
                        )
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().collect(Collectors.toList()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return new ProductSummary.VariantSummary(available, options);
    }
}
