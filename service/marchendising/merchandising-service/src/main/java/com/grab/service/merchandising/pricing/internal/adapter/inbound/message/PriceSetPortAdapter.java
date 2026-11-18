package com.grab.service.merchandising.pricing.internal.adapter.inbound.message;

import com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper.CalculatePricesPortMapper;
import com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper.CreatePricePreferencePortMapper;
import com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper.CreatePriceSetPortMapper;
import com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper.GetPricePreferencePortMapper;
import com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper.GetPriceSetPortMapper;
import com.grab.service.contract.message.merchandising.pricing.PriceSetPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.pricing.application.model.read.CalculatePricesQuery;
import com.pricing.application.model.read.CalculatedPriceSetResult;
import com.pricing.application.model.read.GetPricePreferenceQuery;
import com.pricing.application.model.read.GetPriceSetQuery;
import com.pricing.application.model.write.CreatePricePreferenceCommand;
import com.pricing.application.model.write.CreatePriceSetCommand;
import com.pricing.application.model.write.CreatePriceSetResult;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.model.write.PriceSetResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.grab.rpc.adapter.RmiExport;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RmiExport
public class PriceSetPortAdapter implements PriceSetPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreatePriceSetPortMapper createPriceSetMapper;
    private final GetPriceSetPortMapper getPriceSetMapper;
    private final CalculatePricesPortMapper calculatePricesMapper;
    private final CreatePricePreferencePortMapper createPreferenceMapper;
    private final GetPricePreferencePortMapper getPreferenceMapper;

    public PriceSetPortAdapter(
            @Qualifier("merchandisingCommandBus") CommandBus commandBus,
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            CreatePriceSetPortMapper createPriceSetMapper,
            GetPriceSetPortMapper getPriceSetMapper,
            CalculatePricesPortMapper calculatePricesMapper,
            CreatePricePreferencePortMapper createPreferenceMapper,
            GetPricePreferencePortMapper getPreferenceMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.createPriceSetMapper = createPriceSetMapper;
        this.getPriceSetMapper = getPriceSetMapper;
        this.calculatePricesMapper = calculatePricesMapper;
        this.createPreferenceMapper = createPreferenceMapper;
        this.getPreferenceMapper = getPreferenceMapper;
    }

    @Override
    public PriceSetMessageResult createPriceSet() {
        CreatePriceSetCommand command = createPriceSetMapper.toCommand();
        CreatePriceSetResult result = commandBus.dispatch(command);
        return createPriceSetMapper.toResponse(result);
    }

    @Override
    public PriceSetMessageResult findPriceSetById(Id priceSetId) {
        if (priceSetId == null) {
            return null;
        }
        GetPriceSetQuery query = getPriceSetMapper.toQuery(priceSetId);
        PriceSetResult result = queryBus.dispatch(query);
        return getPriceSetMapper.toResponse(result);
    }

    @Override
    public List<CalculatedPriceMessageResult> calculatePrices(CalculatePriceMessage message) {
        CalculatePricesQuery query = calculatePricesMapper.toQuery(message);
        List<CalculatedPriceSetResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(calculatePricesMapper::toResponse).toList();
    }

    @Override
    public PricePreferenceMessageResult createPricePreference(CreatePricePreferenceMessage message) {
        CreatePricePreferenceCommand command = createPreferenceMapper.toCommand(message);
        PricePreferenceResult result = commandBus.dispatch(command);
        return createPreferenceMapper.toResponse(result);
    }

    @Override
    public PricePreferenceMessageResult findPricePreferenceById(Id preferenceId) {
        if (preferenceId == null) {
            return null;
        }
        GetPricePreferenceQuery query = getPreferenceMapper.toQuery(preferenceId);
        PricePreferenceResult result = queryBus.dispatch(query);
        return getPreferenceMapper.toResponse(result);
    }
}
