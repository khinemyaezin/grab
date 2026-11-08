package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.catalog.port.message.CatalogCategoryPort;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCategoryChildrenMessageMapper {

    public abstract List<CategoryOutboundResult> toResultList(List<CatalogCategoryPort.CategoryMessageResult> results);

    public abstract CategoryOutboundResult toResult(CatalogCategoryPort.CategoryMessageResult result);
}

