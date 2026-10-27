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
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request.AddPriceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request.UpdatePriceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response.PriceSetResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper.AddPriceToPriceSetRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper.CreatePriceSetRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper.UpdatePriceOnPriceSetRequestMapper;
import com.pricing.application.model.write.CreatePriceSetResult;
import com.pricing.application.model.write.DeletePriceSetCommand;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.model.write.RemovePriceFromPriceSetCommand;
import com.grab.app.catalog.pricing.internal.config.PricingEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@PricingEnabled
@RequiredArgsConstructor
public class PriceSetCommandService {
    private static final Logger log = Loggers.getLogger(PriceSetCommandService.class);

    private final CommandBus commandBus;
    private final CreatePriceSetRequestMapper createPriceSetRequestMapper;
    private final AddPriceToPriceSetRequestMapper addPriceToPriceSetRequestMapper;
    private final UpdatePriceOnPriceSetRequestMapper updatePriceOnPriceSetRequestMapper;
    private final IdGenerator idGenerator;

    public String createPriceSet() {
        log.info("Creating price set");
        CreatePriceSetResult result = commandBus.dispatch(createPriceSetRequestMapper.toCommand());
        return createPriceSetRequestMapper.toResponse(result);
    }

    public PriceSetResponse addPrice(String priceSetId, AddPriceRequest request) {
        PriceSetResult result = commandBus.dispatch(addPriceToPriceSetRequestMapper.toCommand(priceSetId, request));
        return addPriceToPriceSetRequestMapper.toResponse(result);
    }

    public PriceSetResponse updatePrice(String priceSetId, String priceId, UpdatePriceRequest request) {
        PriceSetResult result = commandBus.dispatch(
                updatePriceOnPriceSetRequestMapper.toCommand(priceSetId, priceId, request)
        );
        return updatePriceOnPriceSetRequestMapper.toResponse(result);
    }

    public PriceSetResponse removePrice(String priceSetId, String priceId) {
        Id setId = idGenerator.convertIdFrom(priceSetId);
        Id price = idGenerator.convertIdFrom(priceId);
        PriceSetResult result = commandBus.dispatch(new RemovePriceFromPriceSetCommand(setId, price));
        return addPriceToPriceSetRequestMapper.toResponse(result);
    }

    public void deletePriceSet(String priceSetId) {
        Id setId = idGenerator.convertIdFrom(priceSetId);
        commandBus.dispatch(new DeletePriceSetCommand(setId));
    }
}
