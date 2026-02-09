package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.query.ProductSummaryQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdConverter.class)
public abstract class ProductSummaryQueryMapper {

    public abstract ProductSummaryQuery toQuery(String productName,
                                              String sku,
                                              String variantStatus,
                                              List<String> variations,
                                              int page,
                                              int size);
}
