package com.grab.app.platform.seller.internal.adapter.outbound.message;

import com.grab.app.merchandising.pricing.port.message.PriceListPort;
import com.grab.app.merchandising.pricing.port.message.PriceSetPort;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CalculatePricesMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CreatePriceListMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CreatePricePreferenceMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CreatePriceSetMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetPriceListMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetPricePreferenceMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetPriceSetMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ListPriceListsMessageMapper;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class PricingMessageAdapter implements SellerPricingOutboundPort {

    private final PriceListPort priceListPort;
    private final PriceSetPort priceSetPort;
    private final IdGenerator idGenerator;

    private final CreatePriceListMessageMapper createPriceListMapper;
    private final GetPriceListMessageMapper getPriceListMapper;
    private final ListPriceListsMessageMapper listPriceListsMapper;
    private final CreatePriceSetMessageMapper createPriceSetMapper;
    private final GetPriceSetMessageMapper getPriceSetMapper;
    private final CalculatePricesMessageMapper calculatePricesMapper;
    private final CreatePricePreferenceMessageMapper createPreferenceMapper;
    private final GetPricePreferenceMessageMapper getPreferenceMapper;

    public PricingMessageAdapter(
            PriceListPort priceListPort,
            PriceSetPort priceSetPort,
            IdGenerator idGenerator,
            CreatePriceListMessageMapper createPriceListMapper,
            GetPriceListMessageMapper getPriceListMapper,
            ListPriceListsMessageMapper listPriceListsMapper,
            CreatePriceSetMessageMapper createPriceSetMapper,
            GetPriceSetMessageMapper getPriceSetMapper,
            CalculatePricesMessageMapper calculatePricesMapper,
            CreatePricePreferenceMessageMapper createPreferenceMapper,
            GetPricePreferenceMessageMapper getPreferenceMapper
    ) {
        this.priceListPort = priceListPort;
        this.priceSetPort = priceSetPort;
        this.idGenerator = idGenerator;
        this.createPriceListMapper = createPriceListMapper;
        this.getPriceListMapper = getPriceListMapper;
        this.listPriceListsMapper = listPriceListsMapper;
        this.createPriceSetMapper = createPriceSetMapper;
        this.getPriceSetMapper = getPriceSetMapper;
        this.calculatePricesMapper = calculatePricesMapper;
        this.createPreferenceMapper = createPreferenceMapper;
        this.getPreferenceMapper = getPreferenceMapper;
    }

    @Override
    public PriceListOutboundResult createPriceList(CreatePriceListRecord record) {
        PriceListPort.CreatePriceListMessage message = createPriceListMapper.toMessage(record);
        PriceListPort.PriceListMessageResult result = priceListPort.createPriceList(message);
        return createPriceListMapper.toResult(result);
    }

    @Override
    public Optional<PriceListOutboundResult> findPriceListById(String priceListIdStr) {
        Id id = idGenerator.convertIdFrom(priceListIdStr);
        return priceListPort.findPriceListById(id).map(getPriceListMapper::toResult);
    }

    @Override
    public List<PriceListOutboundResult> listPriceLists() {
        List<PriceListPort.PriceListMessageResult> results = priceListPort.listPriceLists();
        return listPriceListsMapper.toResultList(results);
    }

    @Override
    public PriceSetOutboundResult createPriceSet() {
        PriceSetPort.PriceSetMessageResult result = priceSetPort.createPriceSet();
        return createPriceSetMapper.toResult(result);
    }

    @Override
    public Optional<PriceSetOutboundResult> findPriceSetById(String priceSetIdStr) {
        Id id = idGenerator.convertIdFrom(priceSetIdStr);
        return priceSetPort.findPriceSetById(id).map(getPriceSetMapper::toResult);
    }

    @Override
    public List<CalculatedPriceOutboundResult> calculatePrices(CalculatePriceRecord record) {
        List<Id> priceSetIds = record.priceSetIds() == null ? Collections.emptyList()
                : record.priceSetIds().stream().map(idGenerator::convertIdFrom).toList();
        PriceSetPort.CalculatePriceMessage message = calculatePricesMapper.toMessage(record, priceSetIds);
        List<PriceSetPort.CalculatedPriceMessageResult> results = priceSetPort.calculatePrices(message);
        return calculatePricesMapper.toResultList(results);
    }

    @Override
    public PricePreferenceOutboundResult createPricePreference(CreatePricePreferenceRecord record) {
        PriceSetPort.CreatePricePreferenceMessage message = createPreferenceMapper.toMessage(record);
        PriceSetPort.PricePreferenceMessageResult result = priceSetPort.createPricePreference(message);
        return createPreferenceMapper.toResult(result);
    }

    @Override
    public Optional<PricePreferenceOutboundResult> findPricePreference(String preferenceIdStr) {
        Id id = idGenerator.convertIdFrom(preferenceIdStr);
        return priceSetPort.findPricePreferenceById(id).map(getPreferenceMapper::toResult);
    }
}
