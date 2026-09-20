package com.identity.application.model.read;

public record MerchantScopeView(String scopeId, String name, String status) implements MerchantView {
    @Override
    public String getScopeId() {
        return scopeId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getStatus() {
        return status;
    }
}
