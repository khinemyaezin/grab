package com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceSetPort.PriceEntryMessageResult;
import com.grab.app.merchandising.pricing.port.message.PriceSetPort.PriceSetMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.read.GetPriceSetQuery;
import com.pricing.application.model.write.PriceSetResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetPriceSetPortMapper {

    public GetPriceSetQuery toQuery(Id priceSetId) {
        return priceSetId != null ? new GetPriceSetQuery(priceSetId) : null;
    }

    public abstract PriceSetMessageResult toResponse(PriceSetResult result);

    protected abstract PriceEntryMessageResult toEntryResponse(PriceSetResult.PriceResult price);
}

