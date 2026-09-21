package com.customer.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.List;

public interface UserProfileQueryPort {
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
