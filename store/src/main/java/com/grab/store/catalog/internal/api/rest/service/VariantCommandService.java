package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.api.rest.dto.request.BatchVariantImagesRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.SyncVariantsRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateVariantRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.BatchVariantImagesResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteVariantResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.RestoreVariantResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.SyncVariantsResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateVariantResponse;
import com.grab.store.catalog.internal.api.rest.mapper.DeleteVariantDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.RestoreVariantDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.SyncVariantsDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.UpdateVariantDtoMapper;
import com.catalog.application.command.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariantCommandService {

    private static final Logger log = Loggers.getLogger(VariantCommandService.class);

    private final CommandBus commandBus;
    private final UpdateVariantDtoMapper updateVariantDtoMapper;
    private final DeleteVariantDtoMapper deleteVariantDtoMapper;
    private final RestoreVariantDtoMapper restoreVariantDtoMapper;
    private final SyncVariantsDtoMapper syncVariantsDtoMapper;
    private final AuthenticatedCatalogMerchantResolver merchantResolver;
    private final IdGenerator idGenerator;

    public UpdateVariantResponse updateVariant(String productId, String variantId, UpdateVariantRequest request) {
        log.info("Updating variant: {} for product: {}", variantId, productId);

        String merchantId = merchantResolver.resolveCurrentMerchantId();
        UpdateVariantCommand command = updateVariantDtoMapper.toCommand(merchantId, productId, variantId, request);
        UpdateVariantResult result = commandBus.dispatch(command);
        
        return updateVariantDtoMapper.toResponse(result);
    }

    public DeleteVariantResponse deleteVariant(String productId, String variantId) {
        log.info("Deleting variant: {} for product: {}", variantId, productId);

        String merchantId = merchantResolver.resolveCurrentMerchantId();
        DeleteVariantCommand command = deleteVariantDtoMapper.toCommand(merchantId, productId, variantId);
        DeleteVariantResult result = commandBus.dispatch(command);
        
        return deleteVariantDtoMapper.toResponse(result);
    }

    public RestoreVariantResponse restoreVariant(String productId, String variantId) {
        log.info("Restoring variant: {} for product: {}", variantId, productId);

        String merchantId = merchantResolver.resolveCurrentMerchantId();
        RestoreVariantCommand command = restoreVariantDtoMapper.toCommand(merchantId, productId, variantId);
        RestoreVariantResult result = commandBus.dispatch(command);
        
        return restoreVariantDtoMapper.toResponse(result);
    }

    public SyncVariantsResponse syncVariants(String productId, SyncVariantsRequest request) {
        log.info("Syncing variants for product: {}", productId);

        String merchantId = merchantResolver.resolveCurrentMerchantId();
        SyncVariantsCommand command = syncVariantsDtoMapper.toCommand(merchantId, productId, request);
        SyncVariantsResult result = commandBus.dispatch(command);
        
        return syncVariantsDtoMapper.toResponse(result);
    }

    public BatchVariantImagesResponse batchVariantImages(
            String productId,
            String variantId,
            BatchVariantImagesRequest request
    ) {
        log.info("Updating variant images: {} for product: {}", variantId, productId);

        String merchantId = merchantResolver.resolveCurrentMerchantId();
        SetVariantMediaCommand command = new SetVariantMediaCommand(
                idGenerator.convertIdFrom(merchantId),
                idGenerator.convertIdFrom(productId),
                idGenerator.convertIdFrom(variantId),
                request == null || request.mediaIds() == null
                        ? List.of()
                        : request.mediaIds().stream().map(idGenerator::convertIdFrom).toList(),
                request == null || request.thumbnailMediaId() == null || request.thumbnailMediaId().isBlank()
                        ? null
                        : idGenerator.convertIdFrom(request.thumbnailMediaId())
        );
        SetVariantMediaResult result = commandBus.dispatch(command);
        return new BatchVariantImagesResponse(
                result.productId(),
                result.variantId(),
                result.mediaIds(),
                result.thumbnailMediaId()
        );
    }
}
