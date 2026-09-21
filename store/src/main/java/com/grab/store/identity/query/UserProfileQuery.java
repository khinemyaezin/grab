package com.grab.store.identity.query;

import com.grab.framework.id.Id;

import java.util.List;

public interface UserProfileQuery {
    UserProfileResponse getUserProfile(Id userId);

    record UserProfileResponse(
            String id,
            String email,
            String status,
            String createdAt,
            List<String> platformCode
            ) {
    }
}
