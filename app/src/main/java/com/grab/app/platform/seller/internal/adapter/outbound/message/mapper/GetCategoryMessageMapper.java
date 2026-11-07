package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.product.port.message.CatalogCategoryPort;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCategoryMessageMapper {

    public abstract CategoryOutboundResult toResult(CatalogCategoryPort.CategoryMessageResult result);
}

