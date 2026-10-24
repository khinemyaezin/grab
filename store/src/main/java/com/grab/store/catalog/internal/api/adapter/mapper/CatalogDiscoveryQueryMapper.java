package com.grab.store.catalog.internal.api.adapter.mapper;

import com.catalog.application.model.read.GetProductResult;
import com.catalog.application.model.read.PublishedProductChannel;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.id.Id;
import com.grab.store.catalog.port.CatalogDiscoveryQuery.ProductChannelPair;
import com.grab.store.catalog.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.store.catalog.port.CatalogDiscoveryQuery.ProductFacts;
import com.grab.store.catalog.port.CatalogDiscoveryQuery.ProductOption;
import com.grab.store.catalog.port.CatalogDiscoveryQuery.VariantFacts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class CatalogDiscoveryQueryMapper {

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "categoryId", source = "product.category.id")
    @Mapping(target = "title", source = "product.name")
    @Mapping(target = "description", expression = "java(firstDescription(product.descriptions()))")
    @Mapping(target = "thumbnail", expression = "java(firstThumbnail(product.medias()))")
    @Mapping(target = "options", expression = "java(toOptions(product.variantTypes()))")
    @Mapping(target = "variants", expression = "java(toVariants(productId, product.variants()))")
    public abstract ProductFacts toFacts(String productId, String merchantId, GetProductResult product);

    public abstract ProductChannelPair toProductChannelPair(PublishedProductChannel item);

    public abstract List<ProductChannelPair> toProductChannelPairsFromCatalog(List<PublishedProductChannel> items);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "title", source = "variant.matrixKey")
    @Mapping(target = "optionValues", expression = "java(toOptionValues(variant.variations()))")
    @Mapping(target = "salesChannelIds", expression = "java(toSalesChannelIds(variant.publications()))")
    public abstract VariantFacts toVariantFacts(String productId, GetProductResult.Variant variant);

    public List<ProductOption> toOptions(List<GetProductResult.VariantType> variantTypes) {
        if (variantTypes == null) {
            return List.of();
        }
        return variantTypes.stream()
                .sorted(Comparator.comparing(GetProductResult.VariantType::typeId))
                .map(this::toOption)
                .toList();
    }

    public ProductOption toOption(GetProductResult.VariantType variantType) {
        List<String> values = variantType.options().stream()
                .map(GetProductResult.VariantOption::optionName)
                .sorted()
                .toList();
        return new ProductOption(variantType.typeName(), values);
    }

    public List<VariantFacts> toVariants(String productId, List<GetProductResult.Variant> variants) {
        if (variants == null) {
            return List.of();
        }
        return variants.stream().map(variant -> toVariantFacts(productId, variant)).toList();
    }

    public Map<String, String> toOptionValues(List<GetProductResult.Variation> variations) {
        if (variations == null) {
            return Map.of();
        }
        return variations.stream().collect(Collectors.toUnmodifiableMap(
                GetProductResult.Variation::typeName,
                GetProductResult.Variation::optionName,
                (first, second) -> second
        ));
    }

    public abstract List<Id> toIds(List<String> values);

    public List<Id> toSalesChannelIds(List<GetProductResult.Publication> publications) {
        if (publications == null) {
            return List.of();
        }
        List<String> salesChannelIdValues = publications.stream()
                .map(GetProductResult.Publication::salesChannelId)
                .filter(value -> value != null && !value.isBlank())
                .toList();
        return toIds(salesChannelIdValues);
    }

    public String firstDescription(List<GetProductResult.Description> descriptions) {
        if (descriptions == null) {
            return null;
        }
        return descriptions.stream()
                .filter(description -> description.description() != null && !description.description().isBlank())
                .sorted(Comparator.comparing(GetProductResult.Description::id, Comparator.nullsLast(String::compareTo)))
                .map(GetProductResult.Description::description)
                .findFirst()
                .orElse(null);
    }

    public String firstThumbnail(List<GetProductResult.Media> medias) {
        if (medias == null) {
            return null;
        }
        return medias.stream()
                .filter(media -> media.url() != null && !media.url().isBlank())
                .sorted(Comparator.comparingInt(GetProductResult.Media::rank)
                        .thenComparing(GetProductResult.Media::id, Comparator.nullsLast(String::compareTo)))
                .map(GetProductResult.Media::url)
                .findFirst()
                .orElse(null);
    }
}
