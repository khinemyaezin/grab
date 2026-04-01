package com.grab.store.catalog.internal.util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ParentChildTransformer<S, K, P, C> implements Function<Iterable<S>, List<P>> {
    private final Function<? super S, ? extends K> parentKeyExtractor;
    private final Function<? super S, ? extends P> parentFactory;
    private final Function<? super S, ? extends C> childFactory;
    private final BiFunction<? super P, ? super C, ? extends P> childAccumulator;

    private ParentChildTransformer(
            Function<? super S, ? extends K> parentKeyExtractor,
            Function<? super S, ? extends P> parentFactory,
            Function<? super S, ? extends C> childFactory,
            BiFunction<? super P, ? super C, ? extends P> childAccumulator
    ) {
        this.parentKeyExtractor = Objects.requireNonNull(parentKeyExtractor);
        this.parentFactory = Objects.requireNonNull(parentFactory);
        this.childFactory = Objects.requireNonNull(childFactory);
        this.childAccumulator = Objects.requireNonNull(childAccumulator);
    }

    public static <S, K, P, C> ParentChildTransformer<S, K, P, C> of(
            Function<? super S, ? extends K> parentKeyExtractor,
            Function<? super S, ? extends P> parentFactory,
            Function<? super S, ? extends C> childFactory,
            BiFunction<? super P, ? super C, ? extends P> childAccumulator
    ) {
        return new ParentChildTransformer<>(parentKeyExtractor, parentFactory, childFactory, childAccumulator);
    }

    @Override
    public List<P> apply(Iterable<S> source) {
        return group(source);
    }

    public List<P> group(Iterable<S> source) {
        Objects.requireNonNull(source);

        Map<K, P> parents = new LinkedHashMap<>();
        for (S item : source) {
            K parentKey = parentKeyExtractor.apply(item);
            P parent = parents.computeIfAbsent(parentKey, ignored -> parentFactory.apply(item));

            C child = childFactory.apply(item);
            if (child != null) {
                parent = childAccumulator.apply(parent, child);
                parents.put(parentKey, parent);
            }
        }

        return List.copyOf(parents.values());
    }
}
