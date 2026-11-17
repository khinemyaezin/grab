package com.grab.service.sellerbff.internal.adapter.outbound.message;

import lombok.SneakyThrows;
import com.grab.service.contract.message.channel.region.RegionPort;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.GetRegionMessageMapper;
import com.grab.service.sellerbff.port.outbound.SellerRegionOutboundPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RegionMessageAdapter implements SellerRegionOutboundPort {

    private final RegionPort regionPort;
    private final IdGenerator idGenerator;
    private final GetRegionMessageMapper mapper;

    public RegionMessageAdapter(
            RegionPort regionPort,
            IdGenerator idGenerator,
            GetRegionMessageMapper mapper
    ) {
        this.regionPort = regionPort;
        this.idGenerator = idGenerator;
        this.mapper = mapper;
    }

    @Override
    @SneakyThrows
    public Optional<RegionOutboundResult> findRegionById(String regionId) {
        if (regionId == null || regionId.isBlank()) {
            return Optional.empty();
        }
        Id id = idGenerator.convertIdFrom(regionId);
        return java.util.Optional.ofNullable(regionPort.findRegionById(id)).map(mapper::toResult);
    }

    @Override
    @SneakyThrows
    public Optional<RegionOutboundResult> findRegionByCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return Optional.empty();
        }
        return java.util.Optional.ofNullable(regionPort.findRegionByCountryCode(countryCode)).map(mapper::toResult);
    }

    @Override
    @SneakyThrows
    public List<String> listSupportedCountryCodes() {
        return regionPort.listSupportedCountryCodes();
    }
}

