package com.grab.discovery.application.port.outbound;

import com.grab.framework.id.Id;

public interface SalesChannelQueryPort {
    boolean isEnabled(Id salesChannelId);
    String currency(Id salesChannelId);
}
