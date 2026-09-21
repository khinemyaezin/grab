package com.region.application.exception;

import com.grab.framework.exception.DomainException;
import com.grab.framework.exception.MessageSource;

public class RegionServiceException extends DomainException {

    public RegionServiceException(MessageSource messageSource, String defaultMessage) {
        super(messageSource, defaultMessage);
    }

    public RegionServiceException(MessageSource messageSource, String defaultMessage, Throwable cause) {
        super(messageSource, defaultMessage, cause);
    }

    public RegionServiceException(String message, Throwable cause) {
        super(new RegionServiceError.PersistenceFailure(message), message, cause);
    }
}
