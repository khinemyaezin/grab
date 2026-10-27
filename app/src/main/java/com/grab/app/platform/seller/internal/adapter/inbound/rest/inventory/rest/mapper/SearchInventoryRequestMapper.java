package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.SearchInventoryRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.InventoryResponse;
import com.inventory.application.model.read.SearchInventoryQuery;
import com.inventory.application.model.read.SearchInventoryResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchInventoryRequestMapper {

    public abstract SearchInventoryQuery toQuery(String merchantId, SearchInventoryRequest request, Pageable pageable);

    public abstract InventoryResponse toResponse(SearchInventoryResult result);
}
