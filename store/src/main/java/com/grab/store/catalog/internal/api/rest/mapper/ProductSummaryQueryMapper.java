package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.query.ProductSummaryQuery;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ProductSummaryQueryMapper {

    public abstract ProductSummaryQuery toQuery(String productName,
                                              String sku,
                                              String variantStatus,
                                              String categoryId,
                                              String sellerId,
                                              String sellerType,
                                              Boolean offerEligible,
                                              List<String> variations,
                                              int page,
                                              int size);
}
