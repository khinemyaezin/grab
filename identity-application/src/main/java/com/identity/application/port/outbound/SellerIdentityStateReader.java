package com.identity.application.port.outbound;

import com.grab.framework.id.Id;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.application.model.read.AccessContextResult;

import java.util.List;
import java.util.Optional;

public interface SellerIdentityStateReader {
    Optional<AuthenticatedActor> resolve(Id userId, Id assignmentId);

    List<AccessContextResult> listContexts(Id userId);
}
