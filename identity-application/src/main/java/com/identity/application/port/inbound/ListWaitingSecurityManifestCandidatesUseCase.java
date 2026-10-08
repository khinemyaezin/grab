package com.identity.application.port.inbound;

import com.identity.application.model.read.ListWaitingSecurityManifestCandidatesQuery;
import com.identity.application.model.read.WaitingSecurityManifestView;
import java.util.List;

public interface ListWaitingSecurityManifestCandidatesUseCase {
    List<WaitingSecurityManifestView> execute(ListWaitingSecurityManifestCandidatesQuery query);
}
