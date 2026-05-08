package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.service.MatrixCombinationService;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.service.VariationMatrixMatcher;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.impl.DefaultVariationMatrixMatcher;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.store.catalog.internal.query.VariationMatrixQuery;
import com.grab.store.catalog.internal.query.VariationMatrixResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VariationMatrixQueryHandlerTest {
    @Mock
    private MatrixCombinationService matrixCombinationService;
    @Mock
    private MatrixKeyGenerator matrixKeyGenerator;

    private QueryHandler<VariationMatrixQuery, VariationMatrixResult> handler;

    @BeforeEach
    void setUp() {
        VariationMatrixMatcher matcher = new DefaultVariationMatrixMatcher(matrixKeyGenerator);
        handler = new VariationMatrixQueryHandler(
                matrixCombinationService, matrixKeyGenerator, new UuidGenerator(), matcher
        );
    }

    private void mockMatrixKeyGenerator() {
        when(matrixKeyGenerator.generateKey(anyList()))
                .thenAnswer(invocation -> {
                    List<ProductVariation> options = invocation.getArgument(0);
                    return options.stream()
                            .map(option -> option.getOptionId().getValue())
                            .reduce((a, b) -> a + "-" + b)
                            .orElse("");
                });
    }

    private String getTemplate(VariationMatrixResult.Variant v){
        return String.format("%s:%s", v.matrixKey(), v.originalMatrixKey().replace("-",""));
    }

    @Test
    public void handle_deleteVariantManually_returnCombination() {
        // Given
        // (Setup a product with a deleted variation matrix)
        VariationMatrixQuery query = new VariationMatrixQuery(
                List.of(
                        new VariationMatrixQuery.Variant(
                                "y-m-1",
                                List.of(
                                        new VariationMatrixQuery.Variation("y", "c"),
                                        new VariationMatrixQuery.Variation("m", "s"),
                                        new VariationMatrixQuery.Variation("1", "n")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "y-m-2",
                                List.of(
                                        new VariationMatrixQuery.Variation("y", "c"),
                                        new VariationMatrixQuery.Variation("m", "s"),
                                        new VariationMatrixQuery.Variation("2", "n")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "y-s-2",
                                List.of(
                                        new VariationMatrixQuery.Variation("y", "c"),
                                        new VariationMatrixQuery.Variation("s", "s"),
                                        new VariationMatrixQuery.Variation("2", "n")
                                )
                        )
                ),
                List.of(
                        new VariationMatrixQuery.VariantType(
                                "c",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("y"),
                                        new VariationMatrixQuery.VariantOption("b")
                                )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "s",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("m"),
                                        new VariationMatrixQuery.VariantOption("s")
                                )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "n",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("1"),
                                        new VariationMatrixQuery.VariantOption("2")
                                )
                        )
                )
        );

        // When
        mockMatrixKeyGenerator();

        when(matrixCombinationService.generateMatrixCombination(anyList()))
                .thenReturn(List.of(
                        List.of(
                                new VariantOptionSelection(new CommonId("y"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("m"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("y"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("m"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("y"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("y"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("b"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("m"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("b"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("m"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("b"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("b"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("n"))
                        )
                ));

        VariationMatrixResult result = handler.handle(query);

        // Then
        Assertions.assertArrayEquals(
                new String[]{
                        "y-m-1:ym1", "y-m-2:ym2", "y-s-2:ys2", "b-m-1:bm1", "b-m-2:bm2", "b-s-1:bs1", "b-s-2:bs2"
                },
                result.variants()
                        .stream().map(this::getTemplate)
                        .toArray(String[]::new)
        );

    }

    @Test
    public void handle_removeTypeAndAddOption_returnCombination() {
        VariationMatrixQuery query = new VariationMatrixQuery(
                List.of(
                        new VariationMatrixQuery.Variant(
                                "i-y-m-1-s",
                                List.of(
                                        new VariationMatrixQuery.Variation("i", "b"),
                                        new VariationMatrixQuery.Variation("y", "c"),
                                        new VariationMatrixQuery.Variation("m", "s"),
                                        new VariationMatrixQuery.Variation("1", "n"),
                                        new VariationMatrixQuery.Variation("s", "st")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "i-y-m-2-s",
                                List.of(
                                        new VariationMatrixQuery.Variation("i", "b"),
                                        new VariationMatrixQuery.Variation("y", "c"),
                                        new VariationMatrixQuery.Variation("m", "s"),
                                        new VariationMatrixQuery.Variation("2", "n"),
                                        new VariationMatrixQuery.Variation("s", "st")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "i-y-s-2-s",
                                List.of(
                                        new VariationMatrixQuery.Variation("i", "b"),
                                        new VariationMatrixQuery.Variation("y", "c"),
                                        new VariationMatrixQuery.Variation("s", "s"),
                                        new VariationMatrixQuery.Variation("2", "n"),
                                        new VariationMatrixQuery.Variation("s", "st")
                                )
                        )
                ),
                List.of(
                        new VariationMatrixQuery.VariantType(
                                "b",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("i")
                                )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "s",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("m"),
                                        new VariationMatrixQuery.VariantOption("s"),
                                        new VariationMatrixQuery.VariantOption("x")
                                )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "n",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("1"),
                                        new VariationMatrixQuery.VariantOption("2")
                                )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "st",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("s")
                                )
                        )
                )

        );

        //when
        mockMatrixKeyGenerator();

        when(matrixCombinationService.generateMatrixCombination(anyList()))
                .thenReturn(List.of(
                        List.of(
                                new VariantOptionSelection(new CommonId("i"),new CommonId("b")),
                                new VariantOptionSelection(new CommonId("m"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("n")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("st"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("i"),new CommonId("b")),
                                new VariantOptionSelection(new CommonId("m"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("n")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("st"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("i"),new CommonId("b")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("n")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("st"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("i"),new CommonId("b")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("n")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("st"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("i"),new CommonId("b")),
                                new VariantOptionSelection(new CommonId("x"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("n")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("st"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("i"),new CommonId("b")),
                                new VariantOptionSelection(new CommonId("x"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("n")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("st"))
                        )
                ));
        VariationMatrixResult result = handler.handle(query);

        // Then
        Assertions.assertArrayEquals(
                new String[]{
                        "i-m-1-s:iym1s", "i-m-2-s:iym2s", "i-s-2-s:iys2s", "i-x-1-s:ix1s", "i-x-2-s:ix2s"
                },
                result.variants()
                        .stream().map(this::getTemplate)
                        .toArray(String[]::new)
        );
    }

    @Test
    public void handle_addOptionAndRemoveVariant_returnCombination() {
        VariationMatrixQuery query = new VariationMatrixQuery(
                List.of(
                        new VariationMatrixQuery.Variant(
                                "m-1",
                                List.of(
                                        new VariationMatrixQuery.Variation("m", "size"),
                                        new VariationMatrixQuery.Variation("1", "number")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "m-2",
                                List.of(
                                        new VariationMatrixQuery.Variation("m", "size"),
                                        new VariationMatrixQuery.Variation("2", "number")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "s-2",
                                List.of(
                                        new VariationMatrixQuery.Variation("s", "size"),
                                        new VariationMatrixQuery.Variation("2", "number")
                                )
                        )
                ),
                List.of(
                        new VariationMatrixQuery.VariantType(
                                "size",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("m"),
                                        new VariationMatrixQuery.VariantOption("s"),
                                        new VariationMatrixQuery.VariantOption("x")
                                )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "number",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("1"),
                                        new VariationMatrixQuery.VariantOption("2")
                                )
                        )
                )

        );
        //when
        mockMatrixKeyGenerator();

        when(matrixCombinationService.generateMatrixCombination(anyList()))
                .thenReturn(List.of(
                        List.of(
                                new VariantOptionSelection(new CommonId("m"),new CommonId("size")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("number"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("m"),new CommonId("size")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("number"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("s"),new CommonId("size")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("number"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("s"),new CommonId("size")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("number"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("x"),new CommonId("size")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("number"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("x"),new CommonId("size")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("number"))
                        )
                ));
        VariationMatrixResult result = handler.handle(query);

        // Then
        Assertions.assertArrayEquals(
                new String[]{
                        "m-1:m1", "m-2:m2", "s-2:s2", "x-1:x1", "x-2:x2"
                },
                result.variants()
                        .stream().map(this::getTemplate)
                        .toArray(String[]::new)
        );
    }

    @Test
    public void handle_addTypeAndRemoveVariant_returnCombination() {
        VariationMatrixQuery query = new VariationMatrixQuery(
                List.of(
                        new VariationMatrixQuery.Variant(
                                "m-1",
                                List.of(
                                        new VariationMatrixQuery.Variation("m", "s"),
                                        new VariationMatrixQuery.Variation("1", "n")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "m-2",
                                List.of(
                                        new VariationMatrixQuery.Variation("m", "s"),
                                        new VariationMatrixQuery.Variation("2", "n")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "s-2",
                                List.of(
                                        new VariationMatrixQuery.Variation("s", "s"),
                                        new VariationMatrixQuery.Variation("2", "n")
                                )
                        )
                ),
                List.of(
                        new VariationMatrixQuery.VariantType(
                                "c",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("y")
                                )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "s",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("m"),
                                        new VariationMatrixQuery.VariantOption("s")
                                )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "n",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("1"),
                                        new VariationMatrixQuery.VariantOption("2")
                                )
                        )
                )

        );

        //when
        mockMatrixKeyGenerator();

        when(matrixCombinationService.generateMatrixCombination(anyList()))
                .thenReturn(List.of(
                        List.of(
                                new VariantOptionSelection(new CommonId("y"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("m"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("y"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("m"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("y"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("y"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("n"))
                        )
                ));
        VariationMatrixResult result = handler.handle(query);

        // Then
        Assertions.assertArrayEquals(
                new String[]{
                        "y-m-1:m1", "y-m-2:m2", "y-s-2:s2"
                },
                result.variants()
                        .stream().map(this::getTemplate)
                        .toArray(String[]::new)
        );
    }

    @Test
    public void handle_removeOption_returnCombination() {
        VariationMatrixQuery query = new VariationMatrixQuery(
                List.of(
                        new VariationMatrixQuery.Variant(
                                "m-1",
                                List.of(
                                        new VariationMatrixQuery.Variation("m", "s"),
                                        new VariationMatrixQuery.Variation("1", "n")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "m-2",
                                List.of(
                                        new VariationMatrixQuery.Variation("m", "s"),
                                        new VariationMatrixQuery.Variation("2", "n")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "s-2",
                                List.of(
                                        new VariationMatrixQuery.Variation("s", "s"),
                                        new VariationMatrixQuery.Variation("2", "n")
                                )
                        )
                ),
                List.of(
                        new VariationMatrixQuery.VariantType(
                                "s",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("s")
                               )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "n",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("1"),
                                        new VariationMatrixQuery.VariantOption("2")
                                )
                        )
                )

        );

        //when
        mockMatrixKeyGenerator();

        when(matrixCombinationService.generateMatrixCombination(anyList()))
                .thenReturn(List.of(
                        List.of(
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("1"),new CommonId("n"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s")),
                                new VariantOptionSelection(new CommonId("2"),new CommonId("n"))
                        )
                ));
        VariationMatrixResult result = handler.handle(query);

        // Then
        Assertions.assertArrayEquals(
                new String[]{
                        "s-2:s2"
                },
                result.variants()
                        .stream().map(this::getTemplate)
                        .toArray(String[]::new)
        );
    }

    @Test
    public void handle_addTypeOnTopAndRemoveTypeOnEnd_returnCombination() {
        VariationMatrixQuery query = new VariationMatrixQuery(
                List.of(
                        new VariationMatrixQuery.Variant(
                                "m-1",
                                List.of(
                                        new VariationMatrixQuery.Variation("m", "s"),
                                        new VariationMatrixQuery.Variation("1", "n")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "m-2",
                                List.of(
                                        new VariationMatrixQuery.Variation("m", "s"),
                                        new VariationMatrixQuery.Variation("2", "n")
                                )
                        ),
                        new VariationMatrixQuery.Variant(
                                "s-2",
                                List.of(
                                        new VariationMatrixQuery.Variation("s", "s"),
                                        new VariationMatrixQuery.Variation("2", "n")
                                )
                        )
                ),
                List.of(
                        new VariationMatrixQuery.VariantType(
                                "c",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("y")
                                )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "s",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("m"),
                                        new VariationMatrixQuery.VariantOption("s")
                                )
                        )
                )

        );

        //when
        mockMatrixKeyGenerator();

        when(matrixCombinationService.generateMatrixCombination(anyList()))
                .thenReturn(List.of(
                        List.of(
                                new VariantOptionSelection(new CommonId("y"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("m"),new CommonId("s"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("y"),new CommonId("c")),
                                new VariantOptionSelection(new CommonId("s"),new CommonId("s"))
                        )
                ));
        VariationMatrixResult result = handler.handle(query);

        // Then
        Assertions.assertArrayEquals(
                new String[]{
                        "y-m:m1", "y-s:s2"
                },
                result.variants()
                        .stream().map(this::getTemplate)
                        .toArray(String[]::new)
        );
    }

    @Test
    public void handle_withEmptyVariantTypes_shouldReturnEmptyResult() {
        // Given
        VariationMatrixQuery query = new VariationMatrixQuery(
                List.of(),
                List.of()
        );

        // When
        VariationMatrixResult result = handler.handle(query);

        // Then
        Assertions.assertTrue(result.variants().isEmpty());
        Assertions.assertTrue(result.variantTypes().isEmpty());
    }

    @Test
    public void handle_withSingleVariantTypeAndOption_shouldReturnSingleCombination() {
        // Given
        VariationMatrixQuery query = new VariationMatrixQuery(
                List.of(
                        new VariationMatrixQuery.Variant(
                                "yellow",
                                List.of(
                                        new VariationMatrixQuery.Variation("yellow", "color")
                                )
                        )
                ),
                List.of(
                        new VariationMatrixQuery.VariantType(
                                "color",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("yellow")
                                )
                        )
                )
        );

        // When
        mockMatrixKeyGenerator();

        when(matrixCombinationService.generateMatrixCombination(anyList()))
                .thenReturn(List.of(
                        List.of(
                                new VariantOptionSelection(new CommonId("yellow"), new CommonId("color"))
                        )
                ));

        VariationMatrixResult result = handler.handle(query);

        // Then
        Assertions.assertArrayEquals(
                new String[]{
                        "yellow"
                },
                result.variants()
                        .stream().map(VariationMatrixResult.Variant::matrixKey)
                        .toArray(String[]::new)
        );
    }

    @Test
    public void handle_withAllOptionsDifferent_shouldReturnAllMatrixCombinations() {
        // Given - override variants have completely different options from matrix
        VariationMatrixQuery query = new VariationMatrixQuery(
                List.of(
                        new VariationMatrixQuery.Variant(
                                "x-y",
                                List.of(
                                        new VariationMatrixQuery.Variation("x", "t1"),
                                        new VariationMatrixQuery.Variation("y", "t2")
                                )
                        )
                ),
                List.of(
                        new VariationMatrixQuery.VariantType(
                                "t1",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("a"),
                                        new VariationMatrixQuery.VariantOption("b")
                                )
                        ),
                        new VariationMatrixQuery.VariantType(
                                "t2",
                                List.of(
                                        new VariationMatrixQuery.VariantOption("1"),
                                        new VariationMatrixQuery.VariantOption("2")
                                )
                        )
                )
        );

        // When
        mockMatrixKeyGenerator();

        when(matrixCombinationService.generateMatrixCombination(anyList()))
                .thenReturn(List.of(
                        List.of(
                                new VariantOptionSelection(new CommonId("a"), new CommonId("t1")),
                                new VariantOptionSelection(new CommonId("1"), new CommonId("t2"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("a"), new CommonId("t1")),
                                new VariantOptionSelection(new CommonId("2"), new CommonId("t2"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("b"), new CommonId("t1")),
                                new VariantOptionSelection(new CommonId("1"), new CommonId("t2"))
                        ),
                        List.of(
                                new VariantOptionSelection(new CommonId("b"), new CommonId("t1")),
                                new VariantOptionSelection(new CommonId("2"), new CommonId("t2"))
                        )
                ));

        VariationMatrixResult result = handler.handle(query);

        Assertions.assertEquals(4, result.variants().size());
        Assertions.assertArrayEquals(
                new String[]{"a-1", "a-2", "b-1", "b-2"},
                result.variants()
                        .stream().map(VariationMatrixResult.Variant::matrixKey)
                        .toArray(String[]::new)
        );
    }
}
