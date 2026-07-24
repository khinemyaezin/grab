package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.store.pricing.internal.api.rest.dto.request.CreatePricePreferenceRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PricePreferenceResponse;
import com.grab.store.pricing.internal.command.CreatePricePreferenceCommand;
import com.grab.store.pricing.internal.command.PricePreferenceResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreatePricePreferenceRequestMapper {

    public abstract CreatePricePreferenceCommand toCommand(CreatePricePreferenceRequest request);

    public abstract PricePreferenceResponse toResponse(PricePreferenceResult result);
}
