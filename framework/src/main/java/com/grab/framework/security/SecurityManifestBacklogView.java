package com.grab.framework.security;

import java.time.LocalDateTime;

public record SecurityManifestBacklogView(Long pendingCount, LocalDateTime oldestOccurredAt) {
}
