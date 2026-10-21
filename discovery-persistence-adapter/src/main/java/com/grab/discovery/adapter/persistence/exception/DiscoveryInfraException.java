package com.grab.discovery.adapter.persistence.exception;

import com.grab.framework.exception.DomainException;

public class DiscoveryInfraException extends DomainException {

    public DiscoveryInfraException(DiscoveryInfraError error, String defaultMessage) {
        super(error, defaultMessage);
    }

    public DiscoveryInfraException(DiscoveryInfraError error, String defaultMessage, Throwable cause) {
        super(error, defaultMessage, cause);
    }
}
