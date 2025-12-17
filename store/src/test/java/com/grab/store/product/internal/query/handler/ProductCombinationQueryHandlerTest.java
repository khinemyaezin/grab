package com.grab.store.product.internal.query.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.product.internal.assembler.CommonId;
import com.grab.store.product.internal.query.ProductCombinationQuery;
import com.grab.store.product.internal.query.ProductCombinationResult;
import com.product.domain.aggregate.product.VariantOption;
import com.product.domain.aggregate.product.VariantType;
import com.product.domain.service.VariantCombination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCombinationQueryHandlerTest {

    @Mock
    private VariantCombination variantCombinationService;

    @Mock
    private IdGenerator idGenerator;

    private ProductCombinationQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ProductCombinationQueryHandler(variantCombinationService, idGenerator);
    }

    @Test
    void handle_withSingleVariantType_returnsCombinations() {
        // Given
        Id colorTypeId = new CommonId("b7922f9f-1fe9-4e51-9949-982a7398c92a");
        Id redOptionId = new CommonId("94f86eab-8be2-4cee-9303-3340728fcda7");
        Id blueOptionId = new CommonId("128cb594-740c-4735-89f3-c930d2af0b7e");
        Id generatedId = new CommonId("00000000-0000-0000-0000-000000000001");

        ProductCombinationQuery query = new ProductCombinationQuery(
                List.of(new ProductCombinationQuery.VariantType(
                        colorTypeId,
                        "Color",
                        List.of(
                                new ProductCombinationQuery.VariantOption(redOptionId, "Red"),
                                new ProductCombinationQuery.VariantOption(blueOptionId, "Blue")
                        )
                ))
        );

        VariantType domainColorType = new VariantType(colorTypeId, "Color");
        VariantOption domainRed = new VariantOption(redOptionId, "Red", domainColorType);
        VariantOption domainBlue = new VariantOption(blueOptionId, "Blue", domainColorType);

        when(variantCombinationService.generateCombinations(anyList()))
                .thenReturn(List.of(
                        List.of(domainRed),
                        List.of(domainBlue)
                ));
        when(idGenerator.generateId()).thenReturn(generatedId);

        // When
        ProductCombinationResult result = handler.handle(query);

        // Then
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.combinations()).hasSize(2);
        assertThat(result.requestedTypes()).hasSize(1);
        assertThat(result.requestedTypes().getFirst().typeName()).isEqualTo("Color");
        assertThat(result.requestedTypes().getFirst().options()).containsExactly(
                new ProductCombinationResult.VariantOption(redOptionId, "Red"),
                new ProductCombinationResult.VariantOption(blueOptionId, "Blue")
        );
    }

    @Test
    void handle_withMultipleVariantTypes_returnsCartesianProduct() {
        // Given
        Id colorTypeId = new CommonId("b7922f9f-1fe9-4e51-9949-982a7398c92a");
        Id sizeTypeId = new CommonId("71269f58-4d53-4f08-8678-74487ad99217");
        Id redOptionId = new CommonId("94f86eab-8be2-4cee-9303-3340728fcda7");
        Id smallOptionId = new CommonId("128cb594-740c-4735-89f3-c930d2af0b7e");
        Id generatedId = new CommonId("00000000-0000-0000-0000-000000000001");

        ProductCombinationQuery query = new ProductCombinationQuery(
                List.of(
                        new ProductCombinationQuery.VariantType(
                                colorTypeId,
                                "Color",
                                List.of(new ProductCombinationQuery.VariantOption(redOptionId, "Red"))
                        ),
                        new ProductCombinationQuery.VariantType(
                                sizeTypeId,
                                "Size",
                                List.of(new ProductCombinationQuery.VariantOption(smallOptionId, "Small"))
                        )
                )
        );

        VariantType domainColorType = new VariantType(colorTypeId, "Color");
        VariantType domainSizeType = new VariantType(sizeTypeId, "Size");
        VariantOption domainRed = new VariantOption(redOptionId, "Red", domainColorType);
        VariantOption domainSmall = new VariantOption(smallOptionId, "Small", domainSizeType);

        when(variantCombinationService.generateCombinations(anyList()))
                .thenReturn(List.of(List.of(domainRed, domainSmall)));
        when(idGenerator.generateId()).thenReturn(generatedId);

        // When
        ProductCombinationResult result = handler.handle(query);

        // Then
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.combinations()).hasSize(1);
        assertThat(result.requestedTypes()).hasSize(2);

        ProductCombinationResult.VariantCombination combination = result.combinations().get(0);
        assertThat(combination.variations()).hasSize(2);
        assertThat(combination.variations().get(0).optionName()).isEqualTo("Red");
        assertThat(combination.variations().get(1).optionName()).isEqualTo("Small");
    }

    @Test
    void handle_withEmptyVariantTypes_returnsEmptyResult() {
        // Given
        ProductCombinationQuery query = new ProductCombinationQuery(List.of());

        when(variantCombinationService.generateCombinations(anyList()))
                .thenReturn(List.of());

        // When
        ProductCombinationResult result = handler.handle(query);

        // Then
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.combinations()).isEmpty();
        assertThat(result.requestedTypes()).isEmpty();
    }

    @Test
    void handle_verifiesCombinationIdIsGenerated() {
        // Given
        Id colorTypeId = new CommonId("b7922f9f-1fe9-4e51-9949-982a7398c92a");
        Id redOptionId = new CommonId("94f86eab-8be2-4cee-9303-3340728fcda7");
        Id expectedCombinationId = new CommonId("11111111-1111-1111-1111-111111111111");

        ProductCombinationQuery query = new ProductCombinationQuery(
                List.of(new ProductCombinationQuery.VariantType(
                        colorTypeId,
                        "Color",
                        List.of(new ProductCombinationQuery.VariantOption(redOptionId, "Red"))
                ))
        );

        VariantType domainColorType = new VariantType(colorTypeId, "Color");
        VariantOption domainRed = new VariantOption(redOptionId, "Red", domainColorType);

        when(variantCombinationService.generateCombinations(anyList()))
                .thenReturn(List.of(List.of(domainRed)));
        when(idGenerator.generateId()).thenReturn(expectedCombinationId);

        // When
        ProductCombinationResult result = handler.handle(query);

        // Then
        assertThat(result.combinations().get(0).combinationId()).isEqualTo(expectedCombinationId);
    }

    @Test
    void handle_mapsVariationDetailsCorrectly() {
        // Given
        Id colorTypeId = new CommonId("b7922f9f-1fe9-4e51-9949-982a7398c92a");
        Id redOptionId = new CommonId("94f86eab-8be2-4cee-9303-3340728fcda7");
        Id generatedId = new CommonId("00000000-0000-0000-0000-000000000001");

        ProductCombinationQuery query = new ProductCombinationQuery(
                List.of(new ProductCombinationQuery.VariantType(
                        colorTypeId,
                        "Color",
                        List.of(new ProductCombinationQuery.VariantOption(redOptionId, "Red"))
                ))
        );

        VariantType domainColorType = new VariantType(colorTypeId, "Color");
        VariantOption domainRed = new VariantOption(redOptionId, "Red", domainColorType);

        when(variantCombinationService.generateCombinations(anyList()))
                .thenReturn(List.of(List.of(domainRed)));
        when(idGenerator.generateId()).thenReturn(generatedId);

        // When
        ProductCombinationResult result = handler.handle(query);

        // Then
        ProductCombinationResult.Variation variation = result.combinations().getFirst().variations().getFirst();
        assertThat(variation.optionId()).isEqualTo(redOptionId);
        assertThat(variation.optionName()).isEqualTo("Red");
        assertThat(variation.typeId()).isEqualTo(colorTypeId);
        assertThat(variation.typeName()).isEqualTo("Color");
    }

    @Test
    void getQueryType_returnsProductCombinationQueryClass() {
        // When
        Class<ProductCombinationQuery> queryType = handler.getQueryType();

        // Then
        assertThat(queryType).isEqualTo(ProductCombinationQuery.class);
    }
}