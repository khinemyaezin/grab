package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.catalog.CatalogCategoryPort;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetLeafCategoriesMessageMapper {

    public abstract List<CategoryOutboundResult> toResultList(List<CatalogCategoryPort.CategoryMessageResult> results);

    public abstract CategoryOutboundResult toResult(CatalogCategoryPort.CategoryMessageResult result);
}

