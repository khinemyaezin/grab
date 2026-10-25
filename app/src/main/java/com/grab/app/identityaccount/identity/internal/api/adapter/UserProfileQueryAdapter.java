package com.grab.app.identityaccount.identity.internal.api.adapter;

import com.grab.framework.id.Id;
import com.grab.app.identityaccount.identity.internal.api.adapter.mapper.UserProfileQueryMapper;
import com.grab.app.identityaccount.identity.port.UserProfileQuery;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import com.identity.application.port.inbound.GetUserProfileUseCase;
import com.grab.app.identityaccount.identity.internal.config.IdentityReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileQueryAdapter implements UserProfileQuery {
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UserProfileQueryMapper userProfileQueryMapper;

    @Override
    @IdentityReadTransactional
    public UserProfileResponse getUserProfile(Id userId) {
        GetUserProfileResult profile = getUserProfileUseCase.execute(new GetUserProfileQuery(userId));
        return userProfileQueryMapper.toResponse(profile);
    }
}
