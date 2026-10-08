package com.identity.domain.port.outbound;

/** Serializes cross-module manifest activation on the identity catalog singleton. */
public interface SecurityCatalogLock {
    void lockNowait();

    default void recordActivation() {
    }
}
