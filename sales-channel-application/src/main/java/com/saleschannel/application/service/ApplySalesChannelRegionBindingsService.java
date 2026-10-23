package com.saleschannel.application.service;

import com.grab.framework.id.Id;
import com.saleschannel.application.exception.SalesChannelServiceError;
import com.saleschannel.application.exception.SalesChannelServiceException;
import com.saleschannel.application.model.write.ApplySalesChannelRegionBindingsCommand;
import com.saleschannel.application.port.inbound.ApplySalesChannelRegionBindingsUseCase;
import com.saleschannel.application.port.outbound.RegionStatusPort;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class ApplySalesChannelRegionBindingsService implements ApplySalesChannelRegionBindingsUseCase {
    private final SalesChannelRepository salesChannels;
    private final RegionStatusPort regionStatusPort;

    @Override
    public Integer execute(ApplySalesChannelRegionBindingsCommand command) {
        Map<Id, SalesChannel> channels = new HashMap<>();
        Set<Id> defaultChannels = new HashSet<>();
        for (ApplySalesChannelRegionBindingsCommand.Binding binding : command.bindings()) {
            validateRegion(binding.regionId());
            SalesChannel channel = channels.computeIfAbsent(binding.salesChannelId(), this::findChannel);
            if (binding.defaultRegion() && !defaultChannels.add(binding.salesChannelId())) {
                throw new SalesChannelServiceException(
                        new SalesChannelServiceError.MultipleDefaultRegions(binding.salesChannelId().getValue()),
                        "Only one default region can be configured for a sales channel"
                );
            }
            if (binding.enabled()) {
                channel.enableRegion(binding.regionId(), binding.defaultRegion());
            } else {
                channel.disableRegion(binding.regionId());
            }
        }
        for (SalesChannel channel : channels.values()) {
            salesChannels.save(channel);
        }
        return command.bindings().size();
    }

    private void validateRegion(Id regionId) {
        if (!regionStatusPort.isActive(regionId)) {
            throw new SalesChannelServiceException(
                    new SalesChannelServiceError.RegionNotActive(regionId.getValue()),
                    "Region must exist and be active"
            );
        }
    }

    private SalesChannel findChannel(Id salesChannelId) {
        return salesChannels.findById(salesChannelId).orElseThrow(() -> new SalesChannelServiceException(
                new SalesChannelServiceError.ChannelNotFound(salesChannelId.getValue()),
                "Sales channel was not found"
        ));
    }
}
