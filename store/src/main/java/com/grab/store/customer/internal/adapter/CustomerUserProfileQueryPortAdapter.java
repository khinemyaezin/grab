package com.grab.store.customer.internal.adapter;

import com.customer.application.port.outbound.UserProfileQueryPort;
import com.grab.framework.id.Id;
import com.grab.store.identity.port.UserProfileQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerUserProfileQueryPortAdapter implements UserProfileQueryPort {
    private final UserProfileQuery identityUserProfileQuery;

    @Override
    public UserProfileResponse getUserProfile(Id userId) {
        var profile = identityUserProfileQuery.getUserProfile(userId);
        if (profile == null) {
            return null;
        }
        return new UserProfileResponse(
                profile.id(),
                profile.email(),
                profile.status(),
                profile.createdAt(),
                profile.platformCodes()
        );
    }
}
