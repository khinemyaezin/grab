package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.api.rest.assembler.VariantOptionModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.response.VariantOptionResponse;
import com.grab.store.catalog.internal.api.rest.mapper.VariantOptionDtoMapper;
import com.grab.store.catalog.internal.query.GetVariantOptionsByNameQuery;
import com.grab.store.catalog.internal.query.VariantOptionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VariantOptionFacadeService {

    private static final Logger log = Loggers.getLogger(VariantOptionFacadeService.class);

    private final QueryBus queryBus;
    private final VariantOptionDtoMapper variantOptionDtoMapper;
    private final VariantOptionModelAssembler variantOptionModelAssembler;

    public EntityModel<VariantOptionResponse> getVariantOptionsByName(String name, String typeId) {
        log.info("Getting variant options by name: {}", name);

        VariantOptionResult result = queryBus.dispatch(new GetVariantOptionsByNameQuery(name, typeId));
        VariantOptionResponse response = variantOptionDtoMapper.toResponse(result);
        return variantOptionModelAssembler.toModel(response);
    }
}
