package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.product.port.message.CatalogCategoryPort;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryNodeOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCategoryTreeMessageMapper {

    @Mapping(target = "slug", constant = "")
    public abstract CategoryNodeOutboundResult toResult(CatalogCategoryPort.CategoryTreeNodeResult result);
}

