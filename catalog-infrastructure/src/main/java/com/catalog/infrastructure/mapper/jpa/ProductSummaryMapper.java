package com.catalog.infrastructure.mapper.jpa;

import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.mapper.CentralMapperConfig;
import com.catalog.infrastructure.view.ProductSummary;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(config = CentralMapperConfig.class,uses = {IdMapper.class})
public interface ProductSummaryMapper {

    @Mapping(source = "uuid", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "slug", target = "slug")
    @Mapping(source = "featured", target = "featured")
    @Mapping(source = "productVariants", target = "variantSummary", qualifiedByName = "VariantSummary")
    ProductSummary toProductSummary(ProductEntity productEntity);

    @Named("VariantSummary")
    default ProductSummary.VariantSummary toVariantSummary(List<ProductVariantEntity> variants){
        boolean available = variants.stream()
                .anyMatch(v -> "ACTIVE".equals(v.getStatus()));

        List<ProductSummary.VariantType> types = variants.stream()
                .flatMap(v -> v.getProductVariations().stream())
                .collect(Collectors.groupingBy(
                        v -> v.getId().getVariantTypeUuid(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                v -> v.getId().getVariantOptionUuid(),
                                Collectors.toCollection(LinkedHashSet::new)
                        )
                ))
                .entrySet().stream()
                .map(e -> new ProductSummary.VariantType(
                        e.getKey(),
                        e.getValue().stream()
                                .map(ProductSummary.VariantOption::new)
                                .toList()
                ))
                .toList();
        return new ProductSummary.VariantSummary(available, types);
    }
}
