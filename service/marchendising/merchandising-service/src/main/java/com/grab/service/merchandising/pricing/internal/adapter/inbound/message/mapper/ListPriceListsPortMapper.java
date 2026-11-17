package com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.merchandising.pricing.PriceListPort.PriceListMessageResult;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.read.ListPriceListsQuery;
import com.pricing.application.model.write.PriceListResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListPriceListsPortMapper {

    public ListPriceListsQuery toQuery() {
        return new ListPriceListsQuery();
    }

    public abstract PriceListMessageResult toResponse(PriceListResult result);
}
