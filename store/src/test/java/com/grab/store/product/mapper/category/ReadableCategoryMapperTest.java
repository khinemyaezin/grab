package com.grab.store.product.mapper.category;

import com.grab.store_interface.product.dto.category.ReadableCategory;
import com.product.domain.entity.category.AbstractCategory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadableCategoryMapperTest {
    @Test
    public void testConvertWithMerge() {
        ReadableCategoryMapper mapper = new ReadableCategoryMapper() {
            // Implement the abstract class with necessary methods, if needed.
        };
        AbstractCategory mockParentCategory = mock(AbstractCategory.class);
        AbstractCategory mockChildCategory = mock(AbstractCategory.class);

        // Mock parent category
        when(mockParentCategory.getUuid()).thenReturn("uuid-parent");
        when(mockParentCategory.getName()).thenReturn("Parent Category");
        when(mockParentCategory.getDepth()).thenReturn(1);
        when(mockParentCategory.getParent()).thenReturn(null);
        // Arrange - set up the mock behaviors
        when(mockChildCategory.getUuid()).thenReturn("uuid-1");
        when(mockChildCategory.getName()).thenReturn("Category A");
        when(mockChildCategory.getDepth()).thenReturn(0);
        when(mockChildCategory.getParent()).thenReturn(mockParentCategory);

        // Set up mock children
        when(mockParentCategory.getChildren()).thenReturn(Set.of(mockChildCategory));

        // Act - perform the conversion
        ReadableCategory readableCategory = mapper.convert(mockParentCategory);

        assertNotNull(readableCategory);
        assertEquals("uuid-parent", readableCategory.uuid());
        assertEquals("Parent Category", readableCategory.name());
        assertEquals(1, readableCategory.depth());

        // Check child conversion
        assertNotNull(readableCategory.children());
        assertEquals(1, readableCategory.children().size());
    }
}