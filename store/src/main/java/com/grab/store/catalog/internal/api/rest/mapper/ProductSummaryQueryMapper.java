package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.query.ProductSummaryQuery;
import org.mapstruct.Mapper;

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
