package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.request.UpdateVariantRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateVariantResponse;
import com.grab.store.catalog.internal.command.UpdateVariantCommand;
import com.grab.store.catalog.internal.command.UpdateVariantResult;
import com.grab.framework.id.Id;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(config = CentralMapperConfig.class, uses = IdConverter.class)
public abstract class UpdateVariantDtoMapper {
    public abstract UpdateVariantCommand toCommand(String productId, String variantId, UpdateVariantRequest request);

    public abstract UpdateVariantResponse toResponse(UpdateVariantResult result);
}
