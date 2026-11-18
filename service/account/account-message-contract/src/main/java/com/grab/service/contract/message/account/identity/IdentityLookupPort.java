package com.grab.service.contract.message.account.identity;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Set;

/**
 * Universal Java Port Interface for Identity and Actor resolution.
 * Used by perimeter security authenticators without exposing internal identity services.
 */
public interface IdentityLookupPort extends Remote {

    AuthenticatedActorResult resolveByPlatformUserId(
            String issuer,
            String userId,
            AccessContextMessage accessContext
    ) throws RemoteException;

    AuthenticatedActorResult resolveByExternalIdentity(
            String issuer,
            String subject,
            Set<String> entitlements,
            AccessContextMessage accessContext
    ) throws RemoteException;

    record AccessContextMessage(
            String assignmentId,
            String scopeKey,
            String scopeId
    ) implements Serializable {}

    record AuthenticatedActorResult(
            String platformUserId,
            String issuer,
            String subject,
            String email,
            Set<String> roles,
            Set<String> authorities,
            AccessContextMessage accessContext,
            long authenticationVersion
    ) implements Serializable {}
}

