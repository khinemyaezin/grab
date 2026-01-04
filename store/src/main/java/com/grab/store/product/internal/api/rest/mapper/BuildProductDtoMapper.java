package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.api.rest.dto.request.BuildProductRequest;
import com.grab.store.product.internal.api.rest.dto.response.BuildProductResponse;
import com.grab.store.product.internal.query.BuildProductQuery;
import com.grab.store.product.internal.query.BuildProductResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class BuildProductDtoMapper {

    public abstract BuildProductQuery toQuery(BuildProductRequest request);

    protected abstract BuildProductQuery.Product toQueryProduct(BuildProductRequest.Product product);

    protected abstract BuildProductQuery.Variant toQueryVariant(BuildProductRequest.Variant variant);

    protected abstract BuildProductQuery.Variation toQueryVariation(BuildProductRequest.Variation variation);

    protected abstract BuildProductQuery.VariantType toQueryVariantType(BuildProductRequest.VariantType variantType);

    protected abstract BuildProductQuery.VariantOption toQueryVariantOption(BuildProductRequest.VariantOption option);

    public abstract BuildProductResponse toResponse(BuildProductResult result);

    protected abstract BuildProductResponse.Product toResponseProduct(BuildProductResult.Product product);

    protected abstract BuildProductResponse.Variant toResponseVariant(BuildProductResult.Variant variant);

    protected abstract BuildProductResponse.Variation toResponseVariation(BuildProductResult.Variation variation);

    protected abstract BuildProductResponse.VariantType toResponseVariantType(BuildProductResult.VariantType variantType);

    protected abstract BuildProductResponse.VariantOption toResponseVariantOption(BuildProductResult.VariantOption option);
}
