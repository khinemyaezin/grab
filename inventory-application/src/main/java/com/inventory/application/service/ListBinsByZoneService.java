package com.inventory.application.service;

import com.inventory.application.port.inbound.ListBinsByZoneUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.ListBinsByZoneQuery;
import com.inventory.application.model.read.ListBinsResult;
import com.inventory.application.port.outbound.BinQueryPort;
import com.inventory.application.model.read.BinView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class ListBinsByZoneService implements ListBinsByZoneUseCase {
    private final BinQueryPort binRepository;

    private final IdGenerator idGenerator;

            public Page<ListBinsResult> execute(ListBinsByZoneQuery query) {
        Page<BinView> bins;

        if (query.active() != null) {
            bins = binRepository.queryByZoneIdAndActive(query.zoneId().getValue(), query.active(), query.pageable());
        } else {
            bins = binRepository.queryByZoneId(query.zoneId().getValue(), query.pageable());
        }

        return bins.map(this::convertToListBinItem);
    }

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
