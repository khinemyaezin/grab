package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.api.rest.assembler.DeleteVariantModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.RestoreVariantModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.SyncVariantsModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.UpdateVariantModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.request.SyncVariantsRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateVariantRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteVariantResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.RestoreVariantResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.SyncVariantsResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateVariantResponse;
import com.grab.store.catalog.internal.api.rest.mapper.DeleteVariantDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.RestoreVariantDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.SyncVariantsDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateVariantDtoMapper;
import com.grab.store.catalog.internal.command.*;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VariantCommandService {

    private static final Logger log = Loggers.getLogger(VariantCommandService.class);

    private final CommandBus commandBus;
    private final UpdateVariantDtoMapper updateVariantDtoMapper;
    private final DeleteVariantDtoMapper deleteVariantDtoMapper;
    private final RestoreVariantDtoMapper restoreVariantDtoMapper;
    private final SyncVariantsDtoMapper syncVariantsDtoMapper;
    private final UpdateVariantModelAssembler updateVariantModelAssembler;
    private final DeleteVariantModelAssembler deleteVariantModelAssembler;
    private final RestoreVariantModelAssembler restoreVariantModelAssembler;
    private final SyncVariantsModelAssembler syncVariantsModelAssembler;

    public EntityModel<UpdateVariantResponse> updateVariant(String productId, String variantId, UpdateVariantRequest request) {
        log.info("Updating variant: {} for product: {}", variantId, productId);

        UpdateVariantCommand command = updateVariantDtoMapper.toCommand(productId, variantId, request);
        UpdateVariantResult result = commandBus.dispatch(command);
        UpdateVariantResponse response = updateVariantDtoMapper.toResponse(result);

        return updateVariantModelAssembler.toModel(response);
    }

    public EntityModel<DeleteVariantResponse> deleteVariant(String productId, String variantId) {
        log.info("Deleting variant: {} for product: {}", variantId, productId);

        DeleteVariantCommand command = deleteVariantDtoMapper.toCommand(productId, variantId);
        DeleteVariantResult result = commandBus.dispatch(command);
        DeleteVariantResponse response = deleteVariantDtoMapper.toResponse(result);

        return deleteVariantModelAssembler.toModel(response);
    }

    public EntityModel<RestoreVariantResponse> restoreVariant(String productId, String variantId) {
        log.info("Restoring variant: {} for product: {}", variantId, productId);

        RestoreVariantCommand command = restoreVariantDtoMapper.toCommand(productId, variantId);
        RestoreVariantResult result = commandBus.dispatch(command);
        RestoreVariantResponse response = restoreVariantDtoMapper.toResponse(result);

        return restoreVariantModelAssembler.toModel(response);
    }

    public EntityModel<SyncVariantsResponse> syncVariants(String productId, SyncVariantsRequest request) {
        log.info("Syncing variants for product: {}", productId);

        SyncVariantsCommand command = syncVariantsDtoMapper.toCommand(productId, request);
        SyncVariantsResult result = commandBus.dispatch(command);
        SyncVariantsResponse response = syncVariantsDtoMapper.toResponse(result);

        return syncVariantsModelAssembler.toModel(response);
    }
}
