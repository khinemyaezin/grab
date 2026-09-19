package com.grab.store.workflows.internal.workflows.updatesellableproduct;

public final class UpdateSellableProductWorkflowNames {

    public static final String WORKFLOW_NAME = "update-sellable-product";
    public static final String STEP_UPDATE_PRODUCT = "update-product";
    public static final String STEP_SYNC_VARIANT_PRICES = "sync-variant-prices";
    public static final String STEP_SYNC_INVENTORY_ITEM = "sync-inventory-item";
    public static final String STEP_ASSERT_CHANNEL = "assert-channel";
    public static final String STEP_ASSERT_PRODUCT = "assert-product";
    public static final String STEP_ASSERT_CHANNEL_STOCK_PATH = "assert-channel-stock-path";
    public static final String STEP_WRITE_PUBLICATION = "write-publication";
    public static final String STEP_UNPUBLISH_PUBLICATION = "unpublish-publication";

    private UpdateSellableProductWorkflowNames() {
    }
}
