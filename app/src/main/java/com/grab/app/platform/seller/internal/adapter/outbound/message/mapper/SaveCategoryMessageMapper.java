package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.catalog.port.message.CatalogCategoryPort;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.SaveCategoryRecord;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SaveCategoryMessageMapper {

    @Mapping(target = "name", source = "record.name")
    @Mapping(target = "parentId", source = "record.parentId")
    @Mapping(target = "active", source = "record.active", defaultValue = "true")
    @Mapping(target = "listingAllowed", source = "record.listingAllowed", defaultValue = "true")
    @Mapping(target = "c2cAllowed", source = "record.c2cAllowed", defaultValue = "true")
    public abstract CatalogCategoryPort.CreateCategoryMessage toMessage(SaveCategoryRecord record);

    public abstract CategoryOutboundResult toResult(CatalogCategoryPort.CategoryMessageResult result);
}

