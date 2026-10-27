package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request.SyncVariantsRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.SyncVariantsResponse;
import com.catalog.application.model.write.SyncVariantsCommand;
import com.catalog.application.model.write.SyncVariantsResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SyncVariantsDtoMapper {

    public abstract SyncVariantsCommand toCommand(String merchantId, String productId, SyncVariantsRequest request);

    public abstract SyncVariantsResponse toResponse(SyncVariantsResult result);
}
