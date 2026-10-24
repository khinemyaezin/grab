package com.inventory.application.service;

import com.inventory.application.model.write.ProductVariantProjection;
import com.inventory.application.model.write.ProjectProductVariantViewCommand;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.DeleteProduct;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.RenameProduct;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.SetVariantStatus;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.UpsertVariant;
import com.inventory.application.model.write.ProjectProductVariantViewResult;
import com.inventory.application.model.write.ProjectProductVariantViewResult.VariantProjected;
import com.inventory.application.policy.ProductVariantProjectionPolicy;
import com.inventory.application.port.inbound.ProjectProductVariantViewUseCase;
import com.inventory.application.port.outbound.ProductVariantProjectionRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ProjectProductVariantViewService implements ProjectProductVariantViewUseCase {
    private final ProductVariantProjectionRepository projections;
    private final ProductVariantProjectionPolicy policy;

    @Override
    public ProjectProductVariantViewResult execute(ProjectProductVariantViewCommand command) {
        if (command.update() instanceof UpsertVariant update) {
            Optional<ProductVariantProjection> previous = projections.findByVariant(update.variantId());
            ProductVariantProjection saved = policy.upsert(update, previous.orElse(null));
            projections.save(saved);
            VariantProjected notification = new VariantProjected(saved.productId(), saved.variantId(), saved.sku());
            List<VariantProjected> notifications = List.of(notification);
            return new ProjectProductVariantViewResult(notifications);
        }
        if (command.update() instanceof SetVariantStatus update) {
            Optional<ProductVariantProjection> previous = projections.findByVariant(update.variantId());
            previous.ifPresent(projection -> {
                ProductVariantProjection updated = policy.status(projection, update.active());
                projections.save(updated);
            });
        } else if (command.update() instanceof RenameProduct update && policy.canRename(update.productName())) {
            List<ProductVariantProjection> previous = projections.findByProduct(update.productId());
            for (ProductVariantProjection projection : previous) {
                ProductVariantProjection updated = policy.rename(projection, update.productName());
                projections.save(updated);
            }
        } else if (command.update() instanceof DeleteProduct update) {
            List<ProductVariantProjection> previous = projections.findByProduct(update.productId());
            for (ProductVariantProjection projection : previous) {
                ProductVariantProjection updated = policy.status(projection, false);
                projections.save(updated);
            }
        }
        return new ProjectProductVariantViewResult(List.of());
    }
}
