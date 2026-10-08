package com.identity.domain.port.outbound;

/** Identity-side monotonic fence for module authority declarations. */
public interface AuthorityManifestVersionRepository {
    boolean canApply(String moduleKey, int manifestVersion, String contentDigest);

    void recordApplied(String moduleKey, int manifestVersion, String contentDigest);
}
