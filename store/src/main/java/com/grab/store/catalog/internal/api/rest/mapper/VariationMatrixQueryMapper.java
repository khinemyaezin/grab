package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.request.VariationMatrixRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.VariationMatrixResponse;
import com.catalog.application.model.read.VariationMatrixQuery;
import com.catalog.application.model.read.VariationMatrixResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class VariationMatrixQueryMapper {

    public abstract VariationMatrixQuery toQuery(VariationMatrixRequest request);

    public abstract VariationMatrixResponse toResponse(VariationMatrixResult result);
}
