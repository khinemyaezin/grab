package com.identity.application.port.outbound;

import java.util.List;
import java.util.Optional;

public interface ExternalIdentityProviderPort {
    Optional<ExternalIdentityProfile> findByResourceId(String resourceId);

    DirectoryPage listUsers(String pageToken, int pageSize);

    record ExternalIdentityProfile(String resourceId, String subject, String email, boolean enabled, String revision) {
    }

    record DirectoryPage(List<ExternalIdentityProfile> users, String nextPageToken) {
        public DirectoryPage {
            users = List.copyOf(users);
        }

        public boolean complete() {
            return nextPageToken == null || nextPageToken.isBlank();
        }
    }
}
