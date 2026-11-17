package com.grab.service.account.customer.internal.adapter.outbound.message;

import com.customer.application.port.outbound.UserProfileQueryPort;
import com.grab.service.account.customer.internal.config.CustomerTransactional;
import com.grab.service.contract.message.account.identity.IdentityAdminPort;
import com.grab.framework.id.Id;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

@Component
public class CustomerUserProfileQueryAdapter implements UserProfileQueryPort {

    private final IdentityAdminPort identityAdminPort;

    public CustomerUserProfileQueryAdapter(IdentityAdminPort identityAdminPort) {
        this.identityAdminPort = identityAdminPort;
    }

    @CustomerTransactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public UserProfileResponse getUserProfile(Id userId) {
        if (userId == null) {
            return null;
        }
        IdentityAdminPort.UserDetailResult profile;
        try {
            profile = identityAdminPort.findUserById(userId);
        } catch (RemoteException exception) {
            throw new IllegalStateException("Unable to look up the identity profile", exception);
        }
        if (profile == null) {
            return null;
        }
        List<String> roles = profile.accessContexts() != null
                ? profile.accessContexts().stream()
                        .map(IdentityAdminPort.UserDetailResult.AccessContextInfo::roleCode)
                        .toList()
                : Collections.emptyList();
        return new UserProfileResponse(
                profile.id(),
                profile.email(),
                profile.status(),
                profile.createdAt(),
                roles
        );
    }
}

