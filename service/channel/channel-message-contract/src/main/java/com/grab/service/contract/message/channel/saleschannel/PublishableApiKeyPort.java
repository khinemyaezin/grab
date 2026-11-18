package com.grab.service.contract.message.channel.saleschannel;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.time.Instant;

/**
 * Universal Java Port Interface for Publishable API Keys and Token Resolution.
 * Unifies PublishableKeyController and PublishableKeyQuery.
 */
public interface PublishableApiKeyPort extends Remote {

    PublishableKeyResult createPublishableKey(CreatePublishableKeyMessage message) throws RemoteException;

    void revokePublishableKey(Id salesChannelId, Id keyId) throws RemoteException;

    KeyResolutionResult resolveKey(String rawToken) throws RemoteException;

    record CreatePublishableKeyMessage(
            Id salesChannelId,
            String label
    ) implements Serializable {}

    record PublishableKeyResult(
            Id keyId,
            Id salesChannelId,
            String label,
            String rawToken,
            String displayPrefix,
            Instant createdAt
    ) implements Serializable {}

    record KeyResolutionResult(
            Id salesChannelId,
            boolean channelEnabled,
            boolean keyActive
    ) implements Serializable {}
}
