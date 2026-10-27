package com.grab.app.channel.saleschannel.internal.exception;

import com.grab.framework.exception.DomainException;

public class SalesChannelServiceException extends DomainException {

    public SalesChannelServiceException(SalesChannelServiceError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
