package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request.UpdatePricePreferenceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response.PricePreferenceResponse;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.model.write.UpdatePricePreferenceCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdatePricePreferenceRequestMapper {

    @Mapping(target = "pricePreferenceId", source = "pricePreferenceId")
    public abstract UpdatePricePreferenceCommand toCommand(
            String pricePreferenceId,
            UpdatePricePreferenceRequest request
    );

    public abstract PricePreferenceResponse toResponse(PricePreferenceResult result);
}
