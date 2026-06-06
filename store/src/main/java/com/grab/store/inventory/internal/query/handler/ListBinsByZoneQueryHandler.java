package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.ListBinsByZoneQuery;
import com.grab.store.inventory.internal.query.ListBinsResult;
import com.inventory.infrastructure.repository.jpa.BinQueryRepository;
import com.inventory.infrastructure.view.BinView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListBinsByZoneQueryHandler implements QueryHandler<ListBinsByZoneQuery, Page<ListBinsResult>> {
    private final BinQueryRepository binRepository;

    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Page<ListBinsResult> handle(ListBinsByZoneQuery query) {
        Page<BinView> bins;

        if (query.active() != null) {
            bins = binRepository.queryByZoneIdAndActive(query.zoneId().getValue(), query.active(), query.pageable());
        } else {
            bins = binRepository.queryByZoneId(query.zoneId().getValue(), query.pageable());
        }

        return bins.map(this::convertToListBinItem);
    }

    @Override
    public Class<ListBinsByZoneQuery> getQueryType() {
        return ListBinsByZoneQuery.class;
    }

    private ListBinsResult convertToListBinItem(BinView bin) {
        return new ListBinsResult(
                idGenerator.convertIdFrom(bin.uuid()),
                idGenerator.convertIdFrom(bin.zoneId()),
                bin.code(),
                bin.name(),
                bin.maxCapacity(),
                bin.active());
    }
}
