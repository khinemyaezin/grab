package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductStatus;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.aggregate.ProductVariantStatus;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.*;
import com.catalog.domain.service.impl.*;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.SyncVariantsCommand;
import com.grab.store.catalog.internal.command.SyncVariantsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class SyncVariantsCommandHandlerTest {

    private InMemoryProductRepositoryTest productRepository;
    private SyncVariantsCommandHandler handler;

    private final VariantDeletionStrategy variantDeletionStrategy = new FullOptionHardDeleteStrategy();
    private final VariantCombinationService variantCombinationService = new DefaultVariantCombinationService();
    private final VariationKeyGenerator variationKeyGenerator = new DefaultVariationKeyGenerator(new ProductVariationComparator());
    private final VariationCombinationManager variationCombinationManager =
            new DefaultVariationCombinationManager(variationKeyGenerator);

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepositoryTest();
        handler = new SyncVariantsCommandHandler(
                productRepository,
                variantCombinationService,
                variationCombinationManager,
                variationKeyGenerator,
                variantDeletionStrategy
        );
    }

    @Test
    void handle_withOldVariants_shouldMergeNewVariants() {
        Id productId = new CommonId("product-1");
        Product product = Product.create(productId, "T-Shirt", new CommonId("cat-1"));

        ProductVariant redExisting = new ProductVariant(
                new CommonId("var-red"),
                "SKU-OLD-RED",
                ProductVariantStatus.ACTIVE,
                List.of(
                        variation("Red", "opt-red", "Color", "type-color"),
                        variation("Small", "opt-s", "Size", "type-size")
                )
        );
        ProductVariant blueExisting = new ProductVariant(
                new CommonId("var-blue"),
                "SKU-OLD-BLUE",
                ProductVariantStatus.ACTIVE,
                List.of(
                        variation("Blue", "opt-blue", "Color", "type-color"),
                        variation("Small", "opt-s", "Size", "type-size")
                )
        );
        product.addVariant(redExisting);
        product.addVariant(blueExisting);

        productRepository.put(product);

        SyncVariantsCommand command = new SyncVariantsCommand(
                productId,
                List.of(
                        variantType("type-color", "Color",
                                option("opt-red", "Red"),
                                option("opt-blue", "Blue"),
                                option("opt-green", "Green")
                        ),
                        variantType("type-size", "Size",
                                option("opt-s", "Small")
                        )
                ),
                List.of(
                        variant("var-red", "SKU-RED-REQ",
                                cmdVariation("Red", "opt-red", "Color", "type-color"),
                                cmdVariation("Small", "opt-s", "Size", "type-size")
                        ),
                        variant("var-blue", "SKU-BLUE-REQ",
                                cmdVariation("Blue", "opt-blue", "Color", "type-color"),
                                cmdVariation("Small", "opt-s", "Size", "type-size")
                        ),
                        variant("var-green-request", "SKU-GREEN-REQ",
                                cmdVariation("Green", "opt-green", "Color", "type-color"),
                                cmdVariation("Small", "opt-s", "Size", "type-size")
                        )
                )
        );

        SyncVariantsResult result = handler.handle(command);
        Product saved = productRepository.getLastSaved();

        assertThat(saved.getVariants()).hasSize(3);
        assertThat(saved.getVariants())
                .extracting(v -> v.getId().getValue())
                .contains("var-red", "var-blue", "var-green-request");
        assertThat(saved.getVariants())
                .extracting(ProductVariant::getSku)
                .contains("SKU-RED-REQ", "SKU-BLUE-REQ", "SKU-GREEN-REQ");

        assertThat(result.variants()).hasSize(3);
        assertThat(result.variants())
                .extracting(v -> v.id().getValue())
                .contains("var-red", "var-blue", "var-green-request");
    }

    @Test
    void handle_withNewVariants_shouldReplaceOldVariants() {
        Id productId = new CommonId("product-2");
        Product product = Product.create(productId, "T-Shirt", new CommonId("cat-1"));

        ProductVariant redExisting = new ProductVariant(
                new CommonId("var-red"),
                "SKU-RED",
                ProductVariantStatus.ACTIVE,
                List.of(
                        variation("Red", "opt-red", "Color", "type-color"),
                        variation("Small", "opt-s", "Size", "type-size")
                )
        );
        ProductVariant blueExisting = new ProductVariant(
                new CommonId("var-blue"),
                "SKU-BLUE",
                ProductVariantStatus.ACTIVE,
                List.of(
                        variation("Blue", "opt-blue", "Color", "type-color"),
                        variation("Small", "opt-s", "Size", "type-size")
                )
        );
        product.addVariant(redExisting);
        product.addVariant(blueExisting);

        productRepository.put(product);

        SyncVariantsCommand command = new SyncVariantsCommand(
                productId,
                List.of(
                        variantType("type-color", "Color",
                                option("opt-red", "Red")
                        ),
                        variantType("type-size", "Size",
                                option("opt-s", "Small")
                        )
                ),
                List.of(
                        variant("var-red", "SKU-RED-NEW",
                                cmdVariation("Red", "opt-red", "Color", "type-color"),
                                cmdVariation("Small", "opt-s", "Size", "type-size")
                        )
                )
        );

        SyncVariantsResult result = handler.handle(command);
        Product saved = productRepository.getLastSaved();

        assertThat(saved.getVariants()).hasSize(1);
        assertThat(saved.getVariants().getFirst().getId().getValue()).isEqualTo("var-red");
        assertThat(saved.getVariants().getFirst().getSku()).isEqualTo("SKU-RED-NEW");
        assertThat(result.variants()).hasSize(1);
    }

    @Test
    void handle_trustsRequestVariantIdChange_forExistingCombination() {
        Id productId = new CommonId("product-3");
        Product product = Product.create(productId, "T-Shirt", new CommonId("cat-1"));

        product.addVariant(new ProductVariant(
                new CommonId("var-red"),
                "SKU-RED",
                ProductVariantStatus.ACTIVE,
                List.of(
                        variation("Red", "opt-red", "Color", "type-color"),
                        variation("Small", "opt-s", "Size", "type-size")
                )
        ));
        product.addVariant(new ProductVariant(
                new CommonId("var-blue"),
                "SKU-BLUE",
                ProductVariantStatus.ACTIVE,
                List.of(
                        variation("Blue", "opt-blue", "Color", "type-color"),
                        variation("Small", "opt-s", "Size", "type-size")
                )
        ));

        productRepository.put(product);

        SyncVariantsCommand command = new SyncVariantsCommand(
                productId,
                List.of(
                        variantType("type-color", "Color",
                                option("opt-red", "Red"),
                                option("opt-blue", "Blue")
                        ),
                        variantType("type-size", "Size",
                                option("opt-s", "Small")
                        )
                ),
                List.of(
                        variant("var-red-replaced", "SKU-RED-UPDATED",
                                cmdVariation("Red", "opt-red", "Color", "type-color"),
                                cmdVariation("Small", "opt-s", "Size", "type-size")
                        ),
                        variant("var-blue", "SKU-BLUE-UPDATED",
                                cmdVariation("Blue", "opt-blue", "Color", "type-color"),
                                cmdVariation("Small", "opt-s", "Size", "type-size")
                        )
                )
        );

        SyncVariantsResult result = handler.handle(command);
        Product saved = productRepository.getLastSaved();

        assertThat(saved.getVariants()).hasSize(2);
        assertThat(saved.getVariants())
                .extracting(v -> v.getId().getValue())
                .contains("var-red-replaced", "var-blue")
                .doesNotContain("var-red");
        assertThat(saved.getVariants())
                .extracting(ProductVariant::getSku)
                .contains("SKU-RED-UPDATED", "SKU-BLUE-UPDATED");

        assertThat(result.variants())
                .extracting(v -> v.id().getValue())
                .contains("var-red-replaced", "var-blue")
                .doesNotContain("var-red");
    }

    @Test
    void handle_activeProductReplacement_keepsProductSellable() {
        Id productId = new CommonId("product-4");
        Product product = Product.create(productId, "T-Shirt", new CommonId("cat-1"));
        product.addVariant(new ProductVariant(
                new CommonId("var-red"),
                "SKU-RED",
                ProductVariantStatus.ACTIVE,
                List.of(
                        variation("Red", "opt-red", "Color", "type-color"),
                        variation("Small", "opt-s", "Size", "type-size")
                )
        ));
        product.changeStatus(ProductStatus.ACTIVE);
        productRepository.put(product);

        SyncVariantsCommand command = new SyncVariantsCommand(
                productId,
                List.of(
                        variantType("type-color", "Color", option("opt-blue", "Blue")),
                        variantType("type-size", "Size", option("opt-s", "Small"))
                ),
                List.of(
                        variant("var-blue", "SKU-BLUE",
                                cmdVariation("Blue", "opt-blue", "Color", "type-color"),
                                cmdVariation("Small", "opt-s", "Size", "type-size")
                        )
                )
        );

        handler.handle(command);
        Product saved = productRepository.getLastSaved();

        assertThat(saved.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(saved.getVariants()).hasSize(1);
        assertThat(saved.getVariants().getFirst().getId().getValue()).isEqualTo("var-blue");
    }

    @Test
    void handle_activeProductRemovingAllVariants_archivesProduct() {
        Id productId = new CommonId("product-5");
        Product product = Product.create(productId, "T-Shirt", new CommonId("cat-1"));
        product.addVariant(new ProductVariant(
                new CommonId("var-red"),
                "SKU-RED",
                ProductVariantStatus.ACTIVE,
                List.of(
                        variation("Red", "opt-red", "Color", "type-color"),
                        variation("Small", "opt-s", "Size", "type-size")
                )
        ));
        product.changeStatus(ProductStatus.ACTIVE);
        productRepository.put(product);

        SyncVariantsCommand command = new SyncVariantsCommand(productId, List.of(), List.of());

        handler.handle(command);
        Product saved = productRepository.getLastSaved();

        assertThat(saved.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
        assertThat(saved.getVariants()).isEmpty();
    }

    private ProductVariation variation(String optionName, String optionId, String typeName, String typeId) {
        return new ProductVariation(optionName, new CommonId(optionId), typeName, new CommonId(typeId));
    }

    private SyncVariantsCommand.VariantType variantType(
            String typeId,
            String typeName,
            SyncVariantsCommand.VariantOption... options
    ) {
        return new SyncVariantsCommand.VariantType(new CommonId(typeId), typeName, List.of(options));
    }

    private SyncVariantsCommand.VariantOption option(String optionId, String optionName) {
        return new SyncVariantsCommand.VariantOption(new CommonId(optionId), optionName);
    }

    private SyncVariantsCommand.Variant variant(
            String id,
            String sku,
            SyncVariantsCommand.Variation... variations
    ) {
        return new SyncVariantsCommand.Variant(new CommonId(id), sku, List.of(variations));
    }

    private SyncVariantsCommand.Variation cmdVariation(
            String optionName,
            String optionId,
            String typeName,
            String typeId
    ) {
        return new SyncVariantsCommand.Variation(optionName, new CommonId(optionId), new CommonId(typeId), typeName);
    }


}
