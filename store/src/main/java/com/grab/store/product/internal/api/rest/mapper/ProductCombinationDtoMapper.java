package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.product.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.product.internal.query.ProductCombinationQuery;
import com.grab.store.product.internal.query.ProductCombinationResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdConverter.class)
public abstract class ProductCombinationDtoMapper {

    public abstract ProductCombinationQuery toQuery(ProductCombinationRequest dto);

    protected abstract ProductCombinationQuery.VariantType toQueryVariantType(
            ProductCombinationRequest.VariantTypeRequest dto);

    @Mapping(target = "requestedVariantTypes", source = "requestedTypes")
    @Mapping(target = "totalCombinations", source = "totalCount")
    public abstract ProductCombinationResponse toResponse(ProductCombinationResult result);

    protected abstract ProductCombinationResponse.RequestedVariantType toRequestedVariantType(
            ProductCombinationResult.VariantType type);

    protected abstract ProductCombinationResponse.RequestedVariantOption toRequestedVariantOption(
            ProductCombinationResult.VariantOption option);

    @Mapping(target = "id", source = "combinationId")
    protected abstract ProductCombinationResponse.CombinationResponse toResponseCombination(
            ProductCombinationResult.VariantCombination combination);

    protected abstract ProductCombinationResponse.VariationResponse toVariationResponse(
            ProductCombinationResult.Variation variation);
}
