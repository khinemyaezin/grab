package com.grab.store.product.internal.api.rest.service;

import com.grab.framework.id.IdGenerator;
import com.grab.store.product.internal.api.rest.assembler.ProductCombinationModelAssembler;
import com.grab.store.product.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.product.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.product.internal.api.rest.mapper.ProductCombinationDtoMapper;
import com.grab.store.product.internal.cqrs.command.CommandBus;
import com.grab.store.product.internal.cqrs.query.QueryBus;
import com.grab.store.product.internal.query.ProductCombinationQuery;
import com.grab.store.product.internal.query.ProductCombinationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFacadeService {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    private final ProductCombinationDtoMapper combinationDtoMapper;
    private final ProductCombinationModelAssembler combinationModelAssembler;
    private final IdGenerator idGenerator;

    public EntityModel<ProductCombinationResponse> generateCombinations(
            ProductCombinationRequest request) {
        log.info("Generating combinations for {} variant types",
                request.variantTypes().size());

        ProductCombinationQuery query = combinationDtoMapper.toQuery(request);

        ProductCombinationResult result = queryBus.dispatch(query);

        ProductCombinationResponse response = combinationDtoMapper.toResponse(result);

        return combinationModelAssembler.toModel(response);
    }
}
