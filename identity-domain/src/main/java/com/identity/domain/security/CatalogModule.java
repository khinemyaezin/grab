package com.identity.domain.security;

public record CatalogModule(String moduleKey, int appliedRevision, String appliedDigest) {
}
