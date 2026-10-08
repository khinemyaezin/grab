package com.saleschannel.application.port.inbound;

import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;

public interface GetSalesChannelSecurityManifestPublicationStatusUseCase {
    PublicationStatus execute();
}
