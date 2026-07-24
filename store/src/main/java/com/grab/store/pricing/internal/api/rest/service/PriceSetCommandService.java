package com.grab.store.pricing.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import com.grab.store.pricing.internal.api.rest.dto.request.AddPriceRequest;
import com.grab.store.pricing.internal.api.rest.dto.request.UpdatePriceRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceSetResponse;
import com.grab.store.pricing.internal.api.rest.mapper.AddPriceToPriceSetRequestMapper;
import com.grab.store.pricing.internal.api.rest.mapper.CreatePriceSetRequestMapper;
import com.grab.store.pricing.internal.api.rest.mapper.UpdatePriceOnPriceSetRequestMapper;
import com.grab.store.pricing.internal.command.CreatePriceSetResult;
import com.grab.store.pricing.internal.command.DeletePriceSetCommand;
import com.grab.store.pricing.internal.command.PriceSetResult;
import com.grab.store.pricing.internal.command.RemovePriceFromPriceSetCommand;
import com.grab.store.pricing.internal.config.PricingEnabled;
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
