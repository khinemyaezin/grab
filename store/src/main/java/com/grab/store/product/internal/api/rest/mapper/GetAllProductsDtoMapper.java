package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.api.rest.dto.response.GetAllProductsResponse;
import com.grab.store.product.internal.query.GetAllProductsResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GetAllProductsDtoMapper {

    GetAllProductsResponse toResponse(GetAllProductsResult result);

    GetAllProductsResponse.Product toResponseProduct(GetAllProductsResult.Product product);

    GetAllProductsResponse.Variant toResponseVariant(GetAllProductsResult.Variant variant);

    GetAllProductsResponse.Variation toResponseVariation(GetAllProductsResult.Variation variation);
}
