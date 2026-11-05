package com.grab.app.account.customer.internal.adapter;

import com.customer.application.port.outbound.UserProfileQueryPort;
import com.grab.app.account.identity.port.message.IdentityAdminPort;
import com.grab.framework.id.Id;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CustomerUserProfileQueryAdapter implements UserProfileQueryPort {

    private final IdentityAdminPort identityAdminPort;

    public CustomerUserProfileQueryAdapter(IdentityAdminPort identityAdminPort) {
        this.identityAdminPort = identityAdminPort;
    }

    @Override
    public UserProfileResponse getUserProfile(Id userId) {
        if (userId == null) {
            return null;
        }
        var opt = identityAdminPort.findUserById(userId);
        if (opt.isEmpty()) {
            return null;
        }
        var profile = opt.get();
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

