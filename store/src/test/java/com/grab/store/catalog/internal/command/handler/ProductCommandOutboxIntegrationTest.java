package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.*;
import com.catalog.domain.event.*;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.VariantCombinationService;
import com.catalog.domain.service.VariantDeletionStrategy;
import com.catalog.domain.service.VariationCombinationManager;
import com.catalog.domain.service.VariationKeyGenerator;
import com.catalog.domain.service.impl.*;
import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.SellerType;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.mapper.jpa.ProductJpaAssembler;
import com.catalog.infrastructure.outbox.CatalogOutboxEvent;
import com.catalog.infrastructure.outbox.CatalogOutboxEventProducer;
import com.catalog.infrastructure.repository.jpa.ProductJpaRepo;
import com.catalog.infrastructure.repository.jpa.impl.ProductJpaRepository;
import com.catalog.infrastructure.repository.jpa.support.CatalogPersistenceExecutor;
import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.store.catalog.internal.command.*;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCommandOutboxIntegrationTest {

    @Mock
    private ProductJpaRepo productJpaRepo;

    @Mock
    private ProductJpaAssembler productJpaAssembler;

    @Mock
    private OutboxStore<CatalogOutboxEvent, Long> outboxStore;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UniqueSlugResolver uniqueSlugResolver;

    private OutboxEventSerializer serializer;
    private ProductRepository productRepository;

    private VariantDeletionStrategy variantDeletionStrategy;
    private VariantCombinationService variantCombinationService;
    private VariationKeyGenerator variationKeyGenerator;
    private VariationCombinationManager variationCombinationManager;

    @BeforeEach
    void setUp() {
        serializer = new JsonOutboxEventSerializer();
        productRepository = new ProductJpaRepository(
                productJpaAssembler,
                productJpaRepo,
                new CatalogOutboxEventProducer(outboxStore, serializer),
                new CatalogPersistenceExecutor()
        );

        variantDeletionStrategy = new FullOptionHardDeleteStrategy();
        variantCombinationService = new DefaultVariantCombinationService();
        variationKeyGenerator = new DefaultVariationKeyGenerator(new ProductVariationComparator());
        variationCombinationManager = new DefaultVariationCombinationManager(variationKeyGenerator);
    }

    @Test
    void saveProduct_doesNotWriteOutboxRows_untilCreateEventContractExists() {
        SaveProductCommandHandler handler = new SaveProductCommandHandler(
                productRepository,
                categoryRepository,
                uniqueSlugResolver
        );

        Id productId = id("product-create");
        Id categoryId = id("category-1");
        Id variantId = id("variant-1");
        when(categoryRepository.find(categoryId)).thenReturn(Optional.of(Category.createRoot(categoryId, "Category")));
        when(uniqueSlugResolver.resolve(null, "Created Product", null)).thenReturn("created-product");
        when(productJpaRepo.isSkuTaken(anyString(), isNull())).thenReturn(false);
        stubNewAggregateSave(productId);

        handler.handle(new SaveProductCommand(
                new SaveProductCommand.Product(
                        productId,
                        "Created Product",
                        categoryId,
                        null,
                        null,
                        null,
                        false,
                        null,
                        false,
                        List.of(),
                        List.of(),
                        List.of(new SaveProductCommand.Variant(
                                variantId,
                                "SKU-CREATE-1",
                                "ACTIVE",
                                List.of(new SaveProductCommand.Variation(
                                        "Red",
                                        id("opt-red"),
                                        id("type-color"),
                                        "Color"
                                ))
                        ))
                )
        ));

        verify(productJpaRepo).save(any(ProductEntity.class));
        verify(outboxStore, never()).saveAll(any());
    }

    @Test
    void updateMetadataProduct_writesCategoryChangedAndProductUpdatedRows() {
        UpdateProductCommandHandler handler = new UpdateProductCommandHandler(
                productRepository,
                categoryRepository,
                uniqueSlugResolver
        );

        Id productId = id("product-update");
        Id oldCategoryId = id("category-old");
        Id newCategoryId = id("category-new");
        Product product = createProduct(productId, oldCategoryId, "SKU-UPDATE-1");
        stubExistingAggregate(product);
        when(categoryRepository.find(newCategoryId)).thenReturn(Optional.of(Category.createRoot(newCategoryId, "New Category")));
        when(uniqueSlugResolver.resolve(null, "Updated Name", productId.getValue())).thenReturn("updated-name");

        handler.handle(new UpdateProductCommand(
                productId,
                "Updated Name",
                newCategoryId,
                null,
                null,
                null,
                false,
                null,
                true,
                null
        ));

        List<CatalogOutboxEvent> rows = captureOutboxRows();
        assertThat(eventTypes(rows)).containsExactly(
                CategoryChangedEvent.class.getName(),
                ProductUpdatedEvent.class.getName()
        );

        CategoryChangedEvent categoryChangedEvent = (CategoryChangedEvent) deserialize(rows.get(0));
        assertThat(categoryChangedEvent.oldCategory()).isEqualTo(oldCategoryId);
        assertThat(categoryChangedEvent.newCategory()).isEqualTo(newCategoryId);
        assertThat(categoryChangedEvent.productId()).isEqualTo(productId);

        ProductUpdatedEvent productUpdatedEvent = (ProductUpdatedEvent) deserialize(rows.get(1));
        assertThat(productUpdatedEvent.productId()).isEqualTo(productId);
        assertThat(productUpdatedEvent.newName()).isEqualTo("Updated Name");
        assertThat(productUpdatedEvent.newCategoryId()).isEqualTo(newCategoryId);
    }

    @Test
    void updateMetadataProductStatus_writesStatusChangedRow() {
        UpdateProductStatusCommandHandler handler = new UpdateProductStatusCommandHandler(productRepository, categoryRepository);

        Id productId = id("product-status");
        Product product = createProduct(productId, id("category-1"), "SKU-STATUS-1");
        stubExistingAggregate(product);
        when(categoryRepository.find(id("category-1"))).thenReturn(Optional.of(Category.createRoot(id("category-1"), "Category")));

        handler.handle(new UpdateProductStatusCommand(productId, "ACTIVE"));

        List<CatalogOutboxEvent> rows = captureOutboxRows();
        assertThat(eventTypes(rows)).containsExactly(ProductStatusChangedEvent.class.getName());

        ProductStatusChangedEvent event = (ProductStatusChangedEvent) deserialize(rows.getFirst());
        assertThat(event.productId()).isEqualTo(productId);
        assertThat(event.oldStatus()).isEqualTo(ProductStatus.DRAFT.name());
        assertThat(event.newStatus()).isEqualTo(ProductStatus.ACTIVE.name());
    }

    @Test
    void deleteProduct_writesArchiveAndDeleteRows() {
        DeleteProductCommandHandler handler = new DeleteProductCommandHandler(productRepository);

        Id productId = id("product-delete");
        Id categoryId = id("category-1");
        Product product = createProduct(productId, categoryId, "SKU-DELETE-1");
        stubExistingAggregate(product);

        handler.handle(new DeleteProductCommand(productId));

        List<CatalogOutboxEvent> rows = captureOutboxRows();
        assertThat(eventTypes(rows)).containsExactly(
                ProductStatusChangedEvent.class.getName(),
                ProductDeletedEvent.class.getName()
        );

        ProductStatusChangedEvent statusChangedEvent = (ProductStatusChangedEvent) deserialize(rows.get(0));
        assertThat(statusChangedEvent.productId()).isEqualTo(productId);
        assertThat(statusChangedEvent.oldStatus()).isEqualTo(ProductStatus.DRAFT.name());
        assertThat(statusChangedEvent.newStatus()).isEqualTo(ProductStatus.ARCHIVED.name());

        ProductDeletedEvent deletedEvent = (ProductDeletedEvent) deserialize(rows.get(1));
        assertThat(deletedEvent.productId()).isEqualTo(productId);
        assertThat(deletedEvent.categoryId()).isEqualTo(categoryId);
        assertThat(deletedEvent.variantIds()).extracting(Id::getValue).containsExactly("variant-SKU-DELETE-1");
    }

    @Test
    void updateMetadataVariant_writesVariantChangeRow() {
        UpdateVariantCommandHandler handler = new UpdateVariantCommandHandler(productRepository);

        Id productId = id("product-variant-update");
        Product product = createProduct(productId, id("category-1"), "OLD-SKU");
        stubExistingAggregate(product);
        when(productJpaRepo.isSkuTaken("NEW-SKU", "variant-OLD-SKU")).thenReturn(false);

        handler.handle(new UpdateVariantCommand(productId, id("variant-OLD-SKU"), "NEW-SKU"));

        List<CatalogOutboxEvent> rows = captureOutboxRows();
        assertThat(eventTypes(rows)).containsExactly(ProductVariantChangeEvent.class.getName());

        ProductVariantChangeEvent event = (ProductVariantChangeEvent) deserialize(rows.getFirst());
        assertThat(event.sku()).isEqualTo("NEW-SKU");
    }

    @Test
    void deleteVariant_writesVariantDeletedRow() {
        DeleteVariantCommandHandler handler = new DeleteVariantCommandHandler(productRepository);

        Id productId = id("product-variant-delete");
        Id categoryId = id("category-1");
        Product product = createProduct(productId, categoryId, "SKU-DELETE-VARIANT");
        stubExistingAggregate(product);

        handler.handle(new DeleteVariantCommand(productId, id("variant-SKU-DELETE-VARIANT")));

        List<CatalogOutboxEvent> rows = captureOutboxRows();
        assertThat(eventTypes(rows)).containsExactly(ProductVariantDeletedEvent.class.getName());

        ProductVariantDeletedEvent event = (ProductVariantDeletedEvent) deserialize(rows.getFirst());
        assertThat(event.productId()).isEqualTo(productId);
        assertThat(event.categoryId()).isEqualTo(categoryId);
        assertThat(event.variantId()).isEqualTo(id("variant-SKU-DELETE-VARIANT"));
    }

    @Test
    void restoreVariant_writesVariantRestoredRow() {
        RestoreVariantCommandHandler handler = new RestoreVariantCommandHandler(productRepository);

        Id productId = id("product-variant-restore");
        Product product = createProduct(productId, id("category-1"), "SKU-RESTORE");
        product.findVariantById(id("variant-SKU-RESTORE")).orElseThrow().markAsDeleted();
        stubExistingAggregate(product);

        handler.handle(new RestoreVariantCommand(productId, id("variant-SKU-RESTORE")));

        List<CatalogOutboxEvent> rows = captureOutboxRows();
        assertThat(eventTypes(rows)).containsExactly(ProductVariantRestoredEvent.class.getName());

        ProductVariantRestoredEvent event = (ProductVariantRestoredEvent) deserialize(rows.getFirst());
        assertThat(event.productId()).isEqualTo(productId);
        assertThat(event.variantId()).isEqualTo(id("variant-SKU-RESTORE"));
    }

    @Test
    void syncVariants_writesVariantChangeAndDeleteRows() {
        SyncVariantsCommandHandler handler = new SyncVariantsCommandHandler(
                productRepository,
                variantCombinationService,
                variationCombinationManager,
                variationKeyGenerator,
                variantDeletionStrategy
        );

        Id productId = id("product-sync");
        Product product = Product.create(productId, "T-Shirt", id("category-1"));
        product.addVariant(new ProductVariant(
                id("var-red"),
                "SKU-RED-OLD",
                ProductVariantStatus.ACTIVE,
                List.of(
                        variation("Red", "opt-red", "Color", "type-color"),
                        variation("Small", "opt-s", "Size", "type-size")
                )
        ));
        product.addVariant(new ProductVariant(
                id("var-blue"),
                "SKU-BLUE-OLD",
                ProductVariantStatus.ACTIVE,
                List.of(
                        variation("Blue", "opt-blue", "Color", "type-color"),
                        variation("Small", "opt-s", "Size", "type-size")
                )
        ));
        stubExistingAggregate(product);
        when(productJpaRepo.isSkuTaken("SKU-RED-NEW", "var-red")).thenReturn(false);

        handler.handle(new SyncVariantsCommand(
                productId,
                List.of(
                        variantType("type-color", "Color", option("opt-red", "Red")),
                        variantType("type-size", "Size", option("opt-s", "Small"))
                ),
                List.of(
                        variant("var-red", "SKU-RED-NEW",
                                cmdVariation("Red", "opt-red", "Color", "type-color"),
                                cmdVariation("Small", "opt-s", "Size", "type-size"))
                )
        ));

        List<CatalogOutboxEvent> rows = captureOutboxRows();
        assertThat(eventTypes(rows)).containsExactly(
                ProductVariantChangeEvent.class.getName(),
                ProductVariantDeletedEvent.class.getName()
        );

        ProductVariantChangeEvent changedEvent = (ProductVariantChangeEvent) deserialize(rows.get(0));
        assertThat(changedEvent.sku()).isEqualTo("SKU-RED-NEW");

        ProductVariantDeletedEvent deletedEvent = (ProductVariantDeletedEvent) deserialize(rows.get(1));
        assertThat(deletedEvent.productId()).isEqualTo(productId);
        assertThat(deletedEvent.variantId()).isEqualTo(id("var-blue"));
    }

    @Test
    void syncVariants_archivingActiveProduct_writesStatusChangedAndDeleteRows() {
        SyncVariantsCommandHandler handler = new SyncVariantsCommandHandler(
                productRepository,
                variantCombinationService,
                variationCombinationManager,
                variationKeyGenerator,
                variantDeletionStrategy
        );

        Id productId = id("product-sync-archive");
        Product product = createProduct(productId, id("category-1"), "SKU-SYNC-ARCHIVE");
        product.changeStatus(ProductStatus.ACTIVE);
        product.pullEvents();
        stubExistingAggregate(product);

        handler.handle(new SyncVariantsCommand(productId, List.of(), List.of()));

        List<CatalogOutboxEvent> rows = captureOutboxRows();
        assertThat(eventTypes(rows)).containsExactly(
                ProductStatusChangedEvent.class.getName(),
                ProductVariantDeletedEvent.class.getName()
        );

        ProductStatusChangedEvent statusChangedEvent = (ProductStatusChangedEvent) deserialize(rows.get(0));
        assertThat(statusChangedEvent.productId()).isEqualTo(productId);
        assertThat(statusChangedEvent.oldStatus()).isEqualTo(ProductStatus.ACTIVE.name());
        assertThat(statusChangedEvent.newStatus()).isEqualTo(ProductStatus.ARCHIVED.name());
    }

    private void stubNewAggregateSave(Id productId) {
        ProductEntity newEntity = new ProductEntity();
        newEntity.setUuid(productId.getValue());

        when(productJpaRepo.findByUuid(productId.getValue())).thenReturn(Optional.empty());
        when(productJpaAssembler.buildFullEntityGraph(any(Product.class), isNull())).thenReturn(newEntity);
    }

    private void stubExistingAggregate(Product product) {
        ProductEntity existingEntity = new ProductEntity();
        existingEntity.setUuid(product.getId().getValue());

        when(productJpaRepo.findByUuid(product.getId().getValue())).thenReturn(Optional.of(existingEntity));
        when(productJpaAssembler.toFullDomainGraph(existingEntity)).thenReturn(product);
        when(productJpaAssembler.buildFullEntityGraph(any(Product.class), eq(existingEntity))).thenReturn(existingEntity);
    }

    private List<CatalogOutboxEvent> captureOutboxRows() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CatalogOutboxEvent>> captor = ArgumentCaptor.forClass(List.class);
        verify(outboxStore).saveAll(captor.capture());
        List<CatalogOutboxEvent> rows = captor.getValue();
        assertThat(rows).isNotEmpty();
        assertThat(rows).allSatisfy(row -> {
            assertThat(row.getAggregateType()).isEqualTo("Product");
            assertThat(row.getAggregateId()).isNotBlank();
        });
        return rows;
    }

    private List<String> eventTypes(List<CatalogOutboxEvent> rows) {
        return rows.stream().map(CatalogOutboxEvent::getEventType).toList();
    }

    private Event deserialize(CatalogOutboxEvent row) {
        return serializer.deserialize(new SerializedEvent(
                row.getEventType(),
                row.getPayload(),
                row.getEventVersion(),
                row.getHeaders()
        ));
    }

    private Product createProduct(Id productId, Id categoryId, String sku) {
        Product product = Product.create(
                productId,
                "Product " + sku,
                categoryId,
                id("seller-1"),
                SellerType.RETAILER,
                null,
                false,
                false,
                "product-" + sku.toLowerCase(),
                List.of(new Description(null, "default", "Product", "Description")),
                List.of(new ProductMedia(null, "IMAGE", "/images/" + sku.toLowerCase() + ".jpg"))
        );
        product.addVariant(ProductVariant.create(
                id("variant-" + sku),
                sku,
                List.of(new ProductVariation(
                        "Red",
                        id("opt-red"),
                        "Color",
                        id("type-color")
                ))
        ));
        return product;
    }

    private ProductVariation variation(String optionName, String optionId, String typeName, String typeId) {
        return new ProductVariation(optionName, id(optionId), typeName, id(typeId));
    }

    private SyncVariantsCommand.VariantType variantType(
            String typeId,
            String typeName,
            SyncVariantsCommand.VariantOption... options
    ) {
        return new SyncVariantsCommand.VariantType(id(typeId), typeName, List.of(options));
    }

    private SyncVariantsCommand.VariantOption option(String optionId, String optionName) {
        return new SyncVariantsCommand.VariantOption(id(optionId), optionName);
    }

    private SyncVariantsCommand.Variant variant(
            String variantId,
            String sku,
            SyncVariantsCommand.Variation... variations
    ) {
        return new SyncVariantsCommand.Variant(id(variantId), sku, List.of(variations));
    }

    private SyncVariantsCommand.Variation cmdVariation(
            String optionName,
            String optionId,
            String typeName,
            String typeId
    ) {
        return new SyncVariantsCommand.Variation(optionName, id(optionId), id(typeId), typeName);
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}
