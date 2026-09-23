package com.grab.store.identity.port;

import com.grab.framework.id.Id;

import java.util.List;

public interface UserProfileQuery {
    UserProfileResponse getUserProfile(Id userId);

    record UserProfileResponse(
            String id,
            String email,
            String status,
            String createdAt,
            List<String> platformCodes
            ) {
    }
}
