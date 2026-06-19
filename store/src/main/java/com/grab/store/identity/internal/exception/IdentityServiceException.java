package com.grab.store.identity.internal.exception;
import com.grab.framework.exception.DomainException;
public class IdentityServiceException extends DomainException { public IdentityServiceException(IdentityServiceError error,String message){super(error,message);} }
