package com.grab.framework.security;

import java.time.Instant;

public record SecurityManifestBacklogView(Long pendingCount, Instant oldestOccurredAt) {
}
