package com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceListPort.PriceListMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.read.GetPriceListQuery;
import com.pricing.application.model.write.PriceListResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetPriceListPortMapper {

    public GetPriceListQuery toQuery(Id priceListId) {
        return priceListId != null ? new GetPriceListQuery(priceListId) : null;
    }

    public abstract PriceListMessageResult toResponse(PriceListResult result);
}

