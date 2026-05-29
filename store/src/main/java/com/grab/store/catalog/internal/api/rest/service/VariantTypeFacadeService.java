package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.api.rest.assembler.VariantTypeModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.response.VariantTypeResponse;
import com.grab.store.catalog.internal.api.rest.mapper.VariantTypeDtoMapper;
import com.grab.store.catalog.internal.query.GetVariantTypesByNameQuery;
import com.grab.store.catalog.internal.query.VariantTypeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VariantTypeFacadeService {

    private static final Logger log = Loggers.getLogger(VariantTypeFacadeService.class);

    private final QueryBus queryBus;
    private final VariantTypeDtoMapper variantTypeDtoMapper;
    private final VariantTypeModelAssembler variantTypeModelAssembler;

    public EntityModel<VariantTypeResponse> getVariantTypesByName(String name) {
        log.info("Getting variant types by name: {}", name);

        VariantTypeResult result = queryBus.dispatch(new GetVariantTypesByNameQuery(name));
        VariantTypeResponse response = variantTypeDtoMapper.toResponse(result);
        return variantTypeModelAssembler.toModel(response);
    }
}
