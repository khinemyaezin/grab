package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.api.rest.dto.response.ProductSummaryResponse;
import com.grab.store.product.internal.query.ProductSummaryResult;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdConverter.class)
public abstract class ProductSummaryDtoMapper {

  public ProductSummaryResponse toResponse(ProductSummaryResult result ) {
      List<ProductSummaryResponse.Product> products = result.products().stream()
              .map(p -> new ProductSummaryResponse.Product(
                      p.id(),
                      p.name(),
                      new ProductSummaryResponse.VariantSummary(
                              p.variants().available(),
                              p.variants().options()
                      )
              ))
              .toList();
      return new ProductSummaryResponse(products, result.pageInfo());
  }
}
