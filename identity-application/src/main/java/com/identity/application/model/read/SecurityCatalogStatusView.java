package com.identity.application.model.read;

public record SecurityCatalogStatusView(String moduleKey, int appliedRevision, String appliedDigest,
        long catalogRevision, int highestAcceptedRevision, String waitingSince, long conflictCount) {
}
