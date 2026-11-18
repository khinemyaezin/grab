package com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.merchandising.pricing.PriceSetPort.PriceEntryMessageResult;
import com.grab.service.contract.message.merchandising.pricing.PriceSetPort.PriceSetMessageResult;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
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

