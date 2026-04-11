package com.grab.store.catalog.internal.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParentChildTransformerTest {
    @Test
    void transform_groupsRowsByParentId_andAccumulatesChildren() {
        ParentChildTransformer<View, String, Parent, Child> transformer = ParentChildTransformer.of(
                View::parentId,
                view -> new Parent(
                        view.parentId(),
                        view.parentName(),
                        List.of()
                ),
                view -> new Child(view.childId(), view.childName()),
                (parent, child) -> {
                    List<Child> children = new ArrayList<>(parent.children());
                    children.add(child);
                    return new Parent(
                            parent.id(),
                            parent.name(),
                            List.copyOf(children)
                    );
                }
        );

        List<Parent> result = transformer.group(List.of(
                new View("c-1", "Apple", "p-1", "Fruit"),
                new View("c-2", "Banana", "p-1", "Fruit"),
                new View("c-3", "Carrot", "p-2", "Vegetable")
        ));

        assertEquals(List.of(
                new Parent("p-1", "Fruit", List.of(
                        new Child("c-1", "Apple"),
                        new Child("c-2", "Banana")
                )),
                new Parent("p-2", "Vegetable", List.of(
                        new Child("c-3", "Carrot")
                ))
        ), result);
    }

    private record View(
            String childId,
            String childName,
            String parentId,
            String parentName
    ) {
    }

    private record Parent(
            String id,
            String name,
            List<Child> children
    ) {
    }

    private record Child(
            String id,
            String name
    ) {
    }
}