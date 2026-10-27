package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request.CreatePricePreferenceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request.UpdatePricePreferenceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response.PricePreferenceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper.CreatePricePreferenceRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper.UpdatePricePreferenceRequestMapper;
import com.pricing.application.model.write.DeletePricePreferenceCommand;
import com.pricing.application.model.write.PricePreferenceResult;
import com.grab.app.catalog.pricing.internal.config.PricingEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@PricingEnabled
@RequiredArgsConstructor
public class PricePreferenceCommandService {
    private static final Logger log = Loggers.getLogger(PricePreferenceCommandService.class);

    private final CommandBus commandBus;
    private final CreatePricePreferenceRequestMapper createPricePreferenceRequestMapper;
    private final UpdatePricePreferenceRequestMapper updatePricePreferenceRequestMapper;
    private final IdGenerator idGenerator;

    public PricePreferenceResponse create(CreatePricePreferenceRequest request) {
        log.info("Creating price preference for attribute {}", request.attribute());
        PricePreferenceResult result = commandBus.dispatch(createPricePreferenceRequestMapper.toCommand(request));
        return createPricePreferenceRequestMapper.toResponse(result);
    }

    public PricePreferenceResponse update(String pricePreferenceId, UpdatePricePreferenceRequest request) {
        PricePreferenceResult result = commandBus.dispatch(
                updatePricePreferenceRequestMapper.toCommand(pricePreferenceId, request)
        );
        return updatePricePreferenceRequestMapper.toResponse(result);
    }

    public void delete(String pricePreferenceId) {
        Id id = idGenerator.convertIdFrom(pricePreferenceId);
        commandBus.dispatch(new DeletePricePreferenceCommand(id));
    }
}
