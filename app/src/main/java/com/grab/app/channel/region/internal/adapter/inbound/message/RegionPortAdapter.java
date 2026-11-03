package com.grab.app.channel.region.internal.adapter.inbound.message;

import com.grab.app.channel.region.internal.adapter.inbound.message.mapper.FindRegionByCountryPortMapper;
import com.grab.app.channel.region.internal.adapter.inbound.message.mapper.GetRegionPortMapper;
import com.grab.app.channel.region.port.message.RegionPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.region.application.model.read.FindRegionByCountryQuery;
import com.region.application.model.read.GetRegionQuery;
import com.region.application.model.read.ListActiveCountryCodesQuery;
import com.region.application.model.read.RegionResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class RegionPortAdapter implements RegionPort {

    private final QueryBus queryBus;
    private final GetRegionPortMapper getRegionPortMapper;
    private final FindRegionByCountryPortMapper findRegionByCountryPortMapper;

    public RegionPortAdapter(
            @Qualifier("channelQueryBus") QueryBus queryBus,
            GetRegionPortMapper getRegionPortMapper,
            FindRegionByCountryPortMapper findRegionByCountryPortMapper) {
        this.queryBus = queryBus;
        this.getRegionPortMapper = getRegionPortMapper;
        this.findRegionByCountryPortMapper = findRegionByCountryPortMapper;
    }

    @Override
    public Optional<RegionMessageResult> findRegionById(Id regionId) {
        if (regionId == null) {
            return Optional.empty();
        }
        GetRegionQuery query = getRegionPortMapper.toQuery(regionId);
        Optional<RegionResult> result = queryBus.dispatch(query);
        return result.map(getRegionPortMapper::toResponse);
    }

    @Override
    public Optional<RegionMessageResult> findRegionByCountryCode(String countryCode) {
        if (countryCode == null) {
            return Optional.empty();
        }
        FindRegionByCountryQuery query = findRegionByCountryPortMapper.toQuery(countryCode);
        Optional<RegionResult> result = queryBus.dispatch(query);
        return result.map(findRegionByCountryPortMapper::toResponse);
    }

    @Override
    public List<String> listSupportedCountryCodes() {
        ListActiveCountryCodesQuery query = new ListActiveCountryCodesQuery();
        List<String> codes = queryBus.dispatch(query);
        if (codes == null) {
            return Collections.emptyList();
        }
        return codes;
    }
}
