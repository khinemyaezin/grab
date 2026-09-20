package com.inventory.application.model.read;

public interface ProductView {
    String getVariantUuid();

    String getProductUuid();

    String getSku();

    String getProductName();

    String getStatus();

    boolean isManageInventory();
}
