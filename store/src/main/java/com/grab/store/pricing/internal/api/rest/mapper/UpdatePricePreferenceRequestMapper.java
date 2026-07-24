package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.internal.api.rest.dto.request.UpdatePricePreferenceRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PricePreferenceResponse;
import com.grab.store.pricing.internal.command.PricePreferenceResult;
import com.grab.store.pricing.internal.command.UpdatePricePreferenceCommand;
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
