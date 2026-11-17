package com.grab.service.channel.discovery.internal.adapter.outbound.message;

import com.grab.discovery.application.port.outbound.RegionContextPort;
import com.grab.discovery.application.port.outbound.SalesChannelPublishableKeyPort;
import com.grab.discovery.application.port.outbound.SalesChannelQueryPort;
import com.grab.discovery.application.port.outbound.SalesChannelRegionPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.service.channel.discovery.internal.config.DiscoveryReadTransactional;
import com.grab.service.contract.message.channel.region.RegionPort;
import com.grab.service.contract.message.channel.saleschannel.PublishableApiKeyPort;
import com.grab.service.contract.message.channel.saleschannel.SalesChannelContextPort;
import com.saleschannel.application.model.read.FindDefaultSalesChannelRegionQuery;
import com.saleschannel.application.model.read.ServesSalesChannelRegionQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.rmi.RemoteException;
import java.util.Optional;

@Component
@DiscoveryReadTransactional(propagation = Propagation.NOT_SUPPORTED)
public class LocalDiscoveryContextAdapter
        implements RegionContextPort, SalesChannelPublishableKeyPort, SalesChannelQueryPort, SalesChannelRegionPort {
    private final RegionPort regions;
    private final PublishableApiKeyPort keys;
    private final SalesChannelContextPort channels;
    private final QueryBus queries;
    private final IdGenerator ids;

    public LocalDiscoveryContextAdapter(RegionPort regions, PublishableApiKeyPort keys, SalesChannelContextPort channels,
                                        @Qualifier("channelQueryBus") QueryBus queries, IdGenerator ids) {
        this.regions = regions;
        this.keys = keys;
        this.channels = channels;
        this.queries = queries;
        this.ids = ids;
    }

    @Override
    public Optional<RegionView> findByCountry(String countryCode) {
        try {
            return toRegionView(regions.findRegionByCountryCode(countryCode));
        } catch (RemoteException exception) {
            throw new IllegalStateException("Local region lookup failed", exception);
        }
    }

    @Override
    public Optional<RegionView> findById(Id regionId) {
        try {
            return toRegionView(regions.findRegionById(regionId));
        } catch (RemoteException exception) {
            throw new IllegalStateException("Local region lookup failed", exception);
        }
    }

    @Override
    public Optional<ResolvedChannel> resolve(String rawToken) {
        try {
            var result = keys.resolveKey(rawToken);
            if (result == null || !result.keyActive()) {
                return Optional.empty();
            }
            return Optional.of(new ResolvedChannel(result.salesChannelId(), result.channelEnabled()));
        } catch (RemoteException exception) {
            throw new IllegalStateException("Local publishable key lookup failed", exception);
        }
    }

    @Override
    public boolean isEnabled(Id salesChannelId) {
        try {
            return channels.isEnabled(salesChannelId);
        } catch (RemoteException exception) {
            throw new IllegalStateException("Local sales channel lookup failed", exception);
        }
    }

    @Override
    public Optional<Id> findDefaultRegion(Id salesChannelId) {
        var query = new FindDefaultSalesChannelRegionQuery(salesChannelId);
        return queries.dispatch(query);
    }

    @Override
    public boolean servesRegion(Id salesChannelId, Id regionId) {
        var query = new ServesSalesChannelRegionQuery(salesChannelId, regionId);
        return queries.dispatch(query);
    }

    private Optional<RegionView> toRegionView(RegionPort.RegionMessageResult result) {
        if (result == null) {
            return Optional.empty();
        }
        Id regionId = ids.convertIdFrom(result.regionId());
        return Optional.of(new RegionView(regionId, result.currencyCode(), result.active()));
    }
}
