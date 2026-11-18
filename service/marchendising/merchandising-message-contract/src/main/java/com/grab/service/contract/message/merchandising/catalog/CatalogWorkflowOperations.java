package com.grab.service.contract.message.merchandising.catalog;

import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.DescriptionReplacement;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.Eligibility;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.MediaReplacement;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.ProductCreate;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.ProductCreated;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.ProductUpdate;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.ProductUpdated;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.PublicationChanges;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.Publications;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.StatusChange;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.VariantUpdate;
import com.grab.service.contract.event.merchandising.catalog.CatalogWorkflowPayloads.VariantUpdated;
import com.grab.framework.workflow.WorkflowOperation;
import com.grab.framework.workflow.WorkflowUnit;

public final class CatalogWorkflowOperations {

    private CatalogWorkflowOperations() {
    }

    public static final WorkflowOperation<VariantUpdate, VariantUpdated> UPDATE_VARIANT =
            WorkflowOperation.mutation("catalog.update-variant", VariantUpdate.class, VariantUpdated.class);
    public static final WorkflowOperation<ProductCreate, ProductCreated> CREATE_PRODUCT =
            WorkflowOperation.mutation("catalog.create-product-set", ProductCreate.class, ProductCreated.class);
    public static final WorkflowOperation<ProductUpdate, ProductUpdated> UPDATE_PRODUCT =
            WorkflowOperation.mutation("catalog.update-product-set", ProductUpdate.class, ProductUpdated.class);
    public static final WorkflowOperation<MediaReplacement, WorkflowUnit> REPLACE_MEDIA =
            WorkflowOperation.mutation("catalog.replace-media", MediaReplacement.class, WorkflowUnit.class);
    public static final WorkflowOperation<DescriptionReplacement, WorkflowUnit> REPLACE_DESCRIPTIONS =
            WorkflowOperation.mutation("catalog.replace-descriptions", DescriptionReplacement.class, WorkflowUnit.class);
    public static final WorkflowOperation<StatusChange, WorkflowUnit> APPLY_STATUS =
            WorkflowOperation.mutation("catalog.apply-status", StatusChange.class, WorkflowUnit.class);
    public static final WorkflowOperation<Eligibility, WorkflowUnit> ASSERT_ELIGIBILITY =
            WorkflowOperation.assertion("catalog.assert-product-eligibility", Eligibility.class, WorkflowUnit.class);
    public static final WorkflowOperation<Publications, PublicationChanges> PUBLISH =
            WorkflowOperation.mutation("catalog.publish-targets", Publications.class, PublicationChanges.class);
    public static final WorkflowOperation<Publications, PublicationChanges> UNPUBLISH =
            WorkflowOperation.mutation("catalog.unpublish-targets", Publications.class, PublicationChanges.class);
}
