package com.grab.store.catalog.internal.api.rest.controller;

import com.catalog.domain.aggregate.Category;
import com.grab.store.catalog.internal.api.rest.dto.request.SaveCategoryRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryChildrenResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryNodeResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteCategoryResponse;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(CategoryControllerTestConfig.class)
class CategoryControllerIntegrationTest {

    private static final String ROOT_ID = "cat-root";
    private static final String CHILD_A_ID = "cat-child-a";
    private static final String CHILD_B_ID = "cat-child-b";
    private static final String GRANDCHILD_ID = "cat-grand";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryControllerTestConfig.InMemoryCategoryStore store;

    @Test
    void saveCategory_createsRootAndReturnsLocation() throws Exception {
        SaveCategoryRequest request = new SaveCategoryRequest("Shoes", null);

        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertThat(location).isNotNull();

        String id = URI.create(location).getPath().replace("/api/v1/categories/", "");
        assertThat(store.findCategory(id)).isPresent();
        assertThat(store.findCategory(id).get().getName()).isEqualTo("Shoes");
        assertThat(store.findCategory(id).get().isRoot()).isTrue();
    }

    @Test
    void getParentChildrenAndTree_returnExpectedGraph() throws Exception {
        seedCategoryTree();

        CategoryResponse parent = extractCategoryResponse(mockMvc.perform(get("/api/v1/categories/{id}/parent", CHILD_A_ID))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(parent.id()).isEqualTo(ROOT_ID);

        CategoryChildrenResponse children = extractChildrenResponse(mockMvc.perform(get("/api/v1/categories/{id}/children", ROOT_ID))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(children.parentId()).isEqualTo(ROOT_ID);
        assertThat(children.children()).extracting(CategoryResponse::id)
                .containsExactlyInAnyOrder(CHILD_A_ID, CHILD_B_ID);

        CategoryNodeResponse tree = extractTreeResponse(mockMvc.perform(get("/api/v1/categories/{id}/tree", ROOT_ID))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(tree.id()).isEqualTo(ROOT_ID);
        assertThat(tree.children()).hasSize(2);
        CategoryNodeResponse childA = tree.children().stream()
                .filter(child -> CHILD_A_ID.equals(child.id()))
                .findFirst()
                .orElseThrow();
        assertThat(childA.children()).extracting(CategoryNodeResponse::id)
                .containsExactly(GRANDCHILD_ID);
    }

    @Test
    void deleteCategory_removesSubtree() throws Exception {
        seedCategoryTree();

        DeleteCategoryResponse response = extractDeleteResponse(mockMvc.perform(delete("/api/v1/categories/{id}", ROOT_ID))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(response.deleted()).isTrue();
        assertThat(store.findCategory(ROOT_ID)).isEmpty();
        assertThat(store.findCategory(CHILD_A_ID)).isEmpty();
        assertThat(store.findCategory(GRANDCHILD_ID)).isEmpty();
    }

    private void seedCategoryTree() {
        store.save(Category.createRoot(new CommonId(ROOT_ID), "Root"));
        store.save(Category.createChild(new CommonId(CHILD_A_ID), "Child A",
                Category.createRoot(new CommonId(ROOT_ID), "Root")));
        store.save(Category.createChild(new CommonId(CHILD_B_ID), "Child B",
                Category.createRoot(new CommonId(ROOT_ID), "Root")));
        store.save(Category.createChild(new CommonId(GRANDCHILD_ID), "Grand",
                Category.createChild(new CommonId(CHILD_A_ID), "Child A",
                        Category.createRoot(new CommonId(ROOT_ID), "Root"))));
    }

    private CategoryResponse extractCategoryResponse(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        return objectMapper.readValue(json, CategoryResponse.class);
    }

    private CategoryChildrenResponse extractChildrenResponse(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        return objectMapper.readValue(json, CategoryChildrenResponse.class);
    }

    private CategoryNodeResponse extractTreeResponse(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        return objectMapper.readValue(json, CategoryNodeResponse.class);
    }

    private DeleteCategoryResponse extractDeleteResponse(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        return objectMapper.readValue(json, DeleteCategoryResponse.class);
    }
}
