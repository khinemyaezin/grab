package com.grab.store.identity.port;

public interface SecurityCatalogQuery {
    SecurityCatalogStatus status(String moduleKey);

    record SecurityCatalogStatus(String moduleKey, int appliedRevision, String appliedDigest,
            long catalogRevision, int highestAcceptedRevision, String waitingSince, long conflictCount) {
    }
}
