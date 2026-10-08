package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetSecurityCatalogStatusQuery(String moduleKey) implements Query<SecurityCatalogStatusView> {
}
