package com.category.domain.aggregate;

import com.grab.framework.id.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryTest {
    @Test
    @DisplayName("Should create category without parent")
    void shouldCreateCategoryWithoutParent() {
        Id id = mockId("cat-1");
        String name = "Electronics";
        Category category = new Category(id, name);

        // Assert
        assertAll(
                () -> assertEquals(id, category.getId()),
                () -> assertEquals(name, category.getName()),
                () -> assertEquals(Optional.empty(), category.getParentId())
        );
    }

    @Test
    @DisplayName("Should create category with parent")
    void shouldCreateCategoryWithParent() {
        // Arrange
        Id id = mockId("cat-2");
        Id parentId = mockId("parent-1");
        String name = "Laptops";

        // Act
        Category category = new Category(id, name, parentId);

        // Assert
        assertAll(
                () -> assertEquals(id, category.getId()),
                () -> assertEquals(name, category.getName()),
                () -> assertEquals(Optional.of(parentId), category.getParentId())
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when name is null")
    void shouldThrowExceptionWhenNameIsNull() {
        // Arrange
        Id id = mockId("cat-3");

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new Category(id, null));
    }

    @Test
    @DisplayName("Should throw NullPointerException when name is null with parent")
    void shouldThrowExceptionWhenNameIsNullWithParent() {
        // Arrange
        Id id = mockId("cat-4");
        Id parentId = mockId("parent-2");

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new Category(id, null, parentId));
    }

    private Id mockId(String value) {
        Id id = mock(Id.class);
        when(id.getValue()).thenReturn(value);
        return id;
    }

}