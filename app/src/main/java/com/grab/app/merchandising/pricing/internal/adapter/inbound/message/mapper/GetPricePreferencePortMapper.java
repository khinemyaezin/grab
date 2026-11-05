package com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceSetPort.PricePreferenceMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.read.GetPricePreferenceQuery;
import com.pricing.application.model.write.PricePreferenceResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetPricePreferencePortMapper {

    public GetPricePreferenceQuery toQuery(Id preferenceId) {
        return preferenceId != null ? new GetPricePreferenceQuery(preferenceId) : null;
    }

    public abstract PricePreferenceMessageResult toResponse(PricePreferenceResult result);
}

