package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.query.SearchBinsQuery;
import com.grab.store.inventory.internal.query.SearchBinsResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchBinsRequestMapper {

    public abstract SearchBinsQuery toQuery(String merchantId, SearchBinRequest request, Pageable pageable);

    public abstract BinResponse toResponse(SearchBinsResult result);
}
