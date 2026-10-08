package com.identity.application.port.outbound;

import com.identity.application.model.read.WaitingSecurityManifestView;
import com.identity.application.model.read.SecurityCatalogStatusView;
import java.util.List;

public interface SecurityManifestQueryPort {
    List<WaitingSecurityManifestView> findWaiting(int limit);
    SecurityCatalogStatusView status(String moduleKey);
}
