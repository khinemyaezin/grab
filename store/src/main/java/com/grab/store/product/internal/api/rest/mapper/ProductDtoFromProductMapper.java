package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.api.rest.dto.ProductDto;
import com.grab.store.product.internal.api.rest.dto.VariationDto;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface ProductDtoFromProductMapper {
    ProductDto convertProduct(Product product);

    @Mapping(source = "productVariation.variantOption.id", target = "optionId")
    @Mapping(source = "productVariation.variantOption.name", target = "optionName")
    @Mapping(source = "productVariation.variantOption.variantType.id", target = "typeId")
    @Mapping(source = "productVariation.variantOption.variantType.name", target = "typeName")
    VariationDto convertVariation(ProductVariation productVariation);

}
