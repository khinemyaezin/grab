package com.grab.app.merchandising.pricing.internal.adapter.inbound.message;

import com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper.CreatePriceListPortMapper;
import com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper.GetPriceListPortMapper;
import com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper.ListPriceListsPortMapper;
import com.grab.app.merchandising.pricing.port.message.PriceListPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.pricing.application.model.read.GetPriceListQuery;
import com.pricing.application.model.read.ListPriceListsQuery;
import com.pricing.application.model.write.CreatePriceListCommand;
import com.pricing.application.model.write.PriceListResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class PriceListPortAdapter implements PriceListPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreatePriceListPortMapper createPriceListMapper;
    private final GetPriceListPortMapper getPriceListMapper;
    private final ListPriceListsPortMapper listPriceListsMapper;

    public PriceListPortAdapter(
            @Qualifier("merchandisingCommandBus") CommandBus commandBus,
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            CreatePriceListPortMapper createPriceListMapper,
            GetPriceListPortMapper getPriceListMapper,
            ListPriceListsPortMapper listPriceListsMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.createPriceListMapper = createPriceListMapper;
        this.getPriceListMapper = getPriceListMapper;
        this.listPriceListsMapper = listPriceListsMapper;
    }

    @Override
    public PriceListMessageResult createPriceList(CreatePriceListMessage message) {
        CreatePriceListCommand command = createPriceListMapper.toCommand(message);
        PriceListResult result = commandBus.dispatch(command);
        return createPriceListMapper.toResponse(result);
    }

    @Override
    public Optional<PriceListMessageResult> findPriceListById(Id priceListId) {
        if (priceListId == null) {
            return Optional.empty();
        }
        GetPriceListQuery query = getPriceListMapper.toQuery(priceListId);
        PriceListResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getPriceListMapper.toResponse(result));
    }

    @Override
    public List<PriceListMessageResult> listPriceLists() {
        ListPriceListsQuery query = listPriceListsMapper.toQuery();
        List<PriceListResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(listPriceListsMapper::toResponse).toList();
    }
}
