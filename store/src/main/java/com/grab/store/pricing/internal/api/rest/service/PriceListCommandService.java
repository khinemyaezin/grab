package com.grab.store.pricing.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import com.grab.store.pricing.internal.api.rest.dto.request.AddPriceListPriceRequest;
import com.grab.store.pricing.internal.api.rest.dto.request.CreatePriceListRequest;
import com.grab.store.pricing.internal.api.rest.dto.request.ReplacePriceListRulesRequest;
import com.grab.store.pricing.internal.api.rest.dto.request.UpdatePriceListRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceListResponse;
import com.grab.store.pricing.internal.api.rest.mapper.AddPriceToPriceListRequestMapper;
import com.grab.store.pricing.internal.api.rest.mapper.CreatePriceListRequestMapper;
import com.grab.store.pricing.internal.api.rest.mapper.ReplacePriceListRulesRequestMapper;
import com.grab.store.pricing.internal.api.rest.mapper.UpdatePriceListRequestMapper;
import com.grab.store.pricing.internal.command.DeletePriceListCommand;
import com.grab.store.pricing.internal.command.PriceListResult;
import com.grab.store.pricing.internal.command.RemovePriceFromPriceListCommand;
import com.grab.store.pricing.internal.config.PricingEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@PricingEnabled
@RequiredArgsConstructor
public class PriceListCommandService {
    private static final Logger log = Loggers.getLogger(PriceListCommandService.class);

    private final CommandBus commandBus;
    private final CreatePriceListRequestMapper createPriceListRequestMapper;
    private final UpdatePriceListRequestMapper updatePriceListRequestMapper;
    private final ReplacePriceListRulesRequestMapper replacePriceListRulesRequestMapper;
    private final AddPriceToPriceListRequestMapper addPriceToPriceListRequestMapper;
    private final IdGenerator idGenerator;

    public PriceListResponse createPriceList(CreatePriceListRequest request) {
        log.info("Creating price list {}", request.title());
        PriceListResult result = commandBus.dispatch(createPriceListRequestMapper.toCommand(request));
        return createPriceListRequestMapper.toResponse(result);
    }

    public PriceListResponse updatePriceList(String priceListId, UpdatePriceListRequest request) {
        PriceListResult result = commandBus.dispatch(updatePriceListRequestMapper.toCommand(priceListId, request));
        return updatePriceListRequestMapper.toResponse(result);
    }

    public PriceListResponse replaceRules(String priceListId, ReplacePriceListRulesRequest request) {
        PriceListResult result = commandBus.dispatch(
                replacePriceListRulesRequestMapper.toCommand(priceListId, request)
        );
        return replacePriceListRulesRequestMapper.toResponse(result);
    }

    public PriceListResponse addPrice(String priceListId, AddPriceListPriceRequest request) {
        PriceListResult result = commandBus.dispatch(addPriceToPriceListRequestMapper.toCommand(priceListId, request));
        return addPriceToPriceListRequestMapper.toResponse(result);
    }

    public PriceListResponse removePrice(String priceListId, String priceId) {
        Id listId = idGenerator.convertIdFrom(priceListId);
        Id price = idGenerator.convertIdFrom(priceId);
        PriceListResult result = commandBus.dispatch(new RemovePriceFromPriceListCommand(listId, price));
        return addPriceToPriceListRequestMapper.toResponse(result);
    }

    public void deletePriceList(String priceListId) {
        Id listId = idGenerator.convertIdFrom(priceListId);
        commandBus.dispatch(new DeletePriceListCommand(listId));
    }
}
