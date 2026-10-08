package com.grab.store.shared.security;

import com.grab.framework.security.SecurityManifest;
import org.springframework.jdbc.core.JdbcTemplate;

public class SecurityManifestPublicationCoordinator {
    private final JdbcTemplate jdbc;

    public SecurityManifestPublicationCoordinator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean claim(SecurityManifest manifest) {
        int updated = jdbc.update("""
                INSERT INTO security_manifest_publication
                    (module_key, security_revision, content_digest, last_enqueued_at, lease_until, row_version)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '4 minutes', 0)
                ON CONFLICT (module_key) DO UPDATE SET
                    security_revision = EXCLUDED.security_revision,
                    content_digest = EXCLUDED.content_digest,
                    last_enqueued_at = CURRENT_TIMESTAMP,
                    lease_until = CURRENT_TIMESTAMP + INTERVAL '4 minutes',
                    row_version = security_manifest_publication.row_version + 1
                WHERE security_manifest_publication.security_revision < EXCLUDED.security_revision
                   OR security_manifest_publication.content_digest <> EXCLUDED.content_digest
                   OR security_manifest_publication.lease_until IS NULL
                   OR security_manifest_publication.lease_until <= CURRENT_TIMESTAMP
                """, manifest.moduleKey(), manifest.securityRevision(), manifest.contentDigest());
        return updated > 0;
    }
}
