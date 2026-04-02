package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ListingCondition;
import com.catalog.domain.valueobject.ProductMetadata;
import com.catalog.domain.valueobject.SellerType;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.command.GetProductPayload;
import com.grab.store.catalog.internal.command.UpdateProductCommand;
import com.grab.store.catalog.internal.command.UpdateProductResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.CatalogPolicyValidator;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpdateProductCommandHandler implements CommandHandler<UpdateProductCommand, UpdateProductResult> {

    private static final Logger log = Loggers.getLogger(UpdateProductCommandHandler.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UniqueSlugResolver uniqueSlugResolver;

    @Override
    @CatalogTransactional
    public UpdateProductResult handle(UpdateProductCommand command) {
        log.debug("Handling UpdateProductCommand for productId={}", command.productId());

        Optional<Product> hasProduct = productRepository.find(command.productId());
        if (hasProduct.isEmpty()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFound(command.productId().getValue())
            );
        }

        Product product = hasProduct.get();
        Id resolvedCategoryId = command.categoryId() != null ? command.categoryId() : product.getCategoryId();
        Category category = validateCategoryExists(resolvedCategoryId);

        ProductMetadata current = product.metadata();
        ProductMetadata next = new ProductMetadata(
                command.name() != null ? command.name() : current.name(),
                resolvedCategoryId,
                command.sellerId() != null ? command.sellerId() : current.sellerId(),
                mapSellerType(command.sellerType()) != null ? mapSellerType(command.sellerType()) : current.sellerType(),
                mapCondition(command.condition()) != null ? mapCondition(command.condition()) : current.condition(),
                command.offerEligible() != null ? command.offerEligible() : current.offerEligible(),
                command.featured() != null ? command.featured() : current.featured(),
                resolveSlug(command, product),
                command.moderationNote() != null ? command.moderationNote() : current.moderationNote()
        );

        product.updateMetadata(next);
        CatalogPolicyValidator.validateCategoryPolicy(category);

        productRepository.save(product);

        return new UpdateProductResult(
                product.getId().getValue(),
                product.getName(),
                product.getCategoryId().getValue(),
                product.getSellerId() == null ? null : product.getSellerId().getValue(),
                product.getSellerType() == null ? null : product.getSellerType().name(),
                product.getListingCondition() == null ? null : product.getListingCondition().name(),
                product.isOfferEligible(),
                product.getStatus().name(),
                product.getSlug(),
                product.isFeatured(),
                mapPayloadDescriptions(product.getDescriptions()),
                mapPayloadMedias(product.getMedias()),
                product.getModerationNote()
        );
    }

    @Override
    public Class<UpdateProductCommand> getCommandType() {
        return UpdateProductCommand.class;
    }

    private Category validateCategoryExists(Id categoryId) {
        return categoryRepository.find(categoryId).orElseThrow(() -> new CatalogServiceException(
                new CatalogServiceError.CategoryNotFound(categoryId.getValue())
        ));
    }

    private SellerType mapSellerType(String sellerType) {
        return sellerType == null ? null : SellerType.valueOf(sellerType);
    }

    private ListingCondition mapCondition(String condition) {
        return condition == null || condition.isBlank() ? null : ListingCondition.valueOf(condition);
    }

    private List<GetProductPayload.Description> mapPayloadDescriptions(List<Description> descriptions) {
        return descriptions.stream()
                .map(d -> new GetProductPayload.Description(
                        d.getId() == null ? null : new CommonId(d.getId().getValue()),
                        d.getName(),
                        d.getTitle(),
                        d.getDescription()
                ))
                .toList();
    }

    private List<GetProductPayload.Media> mapPayloadMedias(List<ProductMedia> medias) {
        return medias.stream()
                .map(media -> new GetProductPayload.Media(
                        media.getId() == null ? null : new CommonId(media.getId().getValue()),
                        media.getType(),
                        media.getPath()
                ))
                .toList();
    }

    private String resolveSlug(UpdateProductCommand command, Product product) {
        boolean slugProvided = command.slug() != null && !command.slug().isBlank();
        boolean nameProvided = command.name() != null && !command.name().isBlank();

        if (!slugProvided && !nameProvided) {
            return product.getSlug();
        }

        String name = nameProvided ? command.name() : product.getName();
        return uniqueSlugResolver.resolve(command.slug(), name, product.getId().getValue());
    }
}
