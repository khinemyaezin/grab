package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.product.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.product.internal.query.ProductCombinationQuery;
import com.grab.store.product.internal.query.ProductCombinationResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdConverter.class)
public abstract class ProductCombinationDtoMapper {

    public abstract ProductCombinationQuery toQuery(ProductCombinationRequest request);

    protected abstract ProductCombinationQuery.Product toQueryProduct(ProductCombinationRequest.Product product);

    protected abstract ProductCombinationQuery.Variant toQueryVariant(ProductCombinationRequest.Variant variant);

    protected abstract ProductCombinationQuery.Variation toQueryVariation(ProductCombinationRequest.Variation variation);

    protected abstract ProductCombinationQuery.VariantType toQueryVariantType(ProductCombinationRequest.VariantType variantType);

    protected abstract ProductCombinationQuery.VariantOption toQueryVariantOption(ProductCombinationRequest.VariantOption option);

    public abstract ProductCombinationResponse toResponse(ProductCombinationResult result);

    protected abstract ProductCombinationResponse.Product toResponseProduct(ProductCombinationResult.Product product);

    protected abstract ProductCombinationResponse.Variant toResponseVariant(ProductCombinationResult.Variant variant);

    protected abstract ProductCombinationResponse.Variation toResponseVariation(ProductCombinationResult.Variation variation);

    protected abstract ProductCombinationResponse.VariantType toResponseVariantType(ProductCombinationResult.VariantType variantType);

    protected abstract ProductCombinationResponse.VariantOption toResponseVariantOption(ProductCombinationResult.VariantOption option);
}
