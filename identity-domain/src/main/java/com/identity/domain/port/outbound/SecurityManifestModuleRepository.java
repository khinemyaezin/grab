package com.identity.domain.port.outbound;

public interface SecurityManifestModuleRepository {
    int appliedRevision(String moduleKey);

    void recordApplied(String moduleKey, int revision, String digest);
}
