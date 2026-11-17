package com.grab.service.contract.event.channel.saleschannel;

import com.grab.framework.id.Id;

import java.util.List;

public final class SalesChannelWorkflowPayloads {

    private SalesChannelWorkflowPayloads() {
    }

    public record PublicationChannels(Id merchantId, List<Id> publishing, List<Id> unpublishing) {
        public PublicationChannels {
            publishing = List.copyOf(publishing);
            unpublishing = List.copyOf(unpublishing);
        }
    }
}
