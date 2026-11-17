package com.identity.application.model.read;

import com.grab.framework.id.Id;

public record VerifiedSeller(Id userId, long authenticationVersion) {}
