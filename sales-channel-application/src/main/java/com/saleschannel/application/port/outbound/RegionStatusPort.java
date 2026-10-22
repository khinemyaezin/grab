package com.saleschannel.application.port.outbound;

import com.grab.framework.id.Id;

public interface RegionStatusPort {
    boolean isActive(Id regionId);
}
