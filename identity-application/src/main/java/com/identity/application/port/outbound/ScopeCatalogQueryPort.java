package com.identity.application.port.outbound;

import com.identity.application.model.read.ScopeCatalogView;

public interface ScopeCatalogQueryPort {
    ScopeCatalogView load();
}
