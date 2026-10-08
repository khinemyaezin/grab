package com.identity.application.service;

import com.identity.application.model.read.ListWaitingSecurityManifestCandidatesQuery;
import com.identity.application.model.read.WaitingSecurityManifestView;
import com.identity.application.port.inbound.ListWaitingSecurityManifestCandidatesUseCase;
import com.identity.application.port.outbound.SecurityManifestQueryPort;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class ListWaitingSecurityManifestCandidatesService implements ListWaitingSecurityManifestCandidatesUseCase {
    private final SecurityManifestQueryPort manifests;

    @Override
    public List<WaitingSecurityManifestView> execute(ListWaitingSecurityManifestCandidatesQuery query) {
        return manifests.findWaiting(query.limit());
    }
}
