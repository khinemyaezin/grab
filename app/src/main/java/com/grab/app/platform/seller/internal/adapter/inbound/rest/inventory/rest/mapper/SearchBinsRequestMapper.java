package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.SearchBinRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.BinResponse;
import com.inventory.application.model.read.SearchBinsQuery;
import com.inventory.application.model.read.SearchBinsResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchBinsRequestMapper {

    public abstract SearchBinsQuery toQuery(String merchantId, SearchBinRequest request, Pageable pageable);

    public abstract BinResponse toResponse(SearchBinsResult result);
}
