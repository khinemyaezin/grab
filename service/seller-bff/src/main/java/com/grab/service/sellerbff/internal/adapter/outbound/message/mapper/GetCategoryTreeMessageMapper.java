package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.catalog.CatalogCategoryPort;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort.CategoryNodeOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCategoryTreeMessageMapper {

    @Mapping(target = "slug", constant = "")
    public abstract CategoryNodeOutboundResult toResult(CatalogCategoryPort.CategoryTreeNodeResult result);
}

