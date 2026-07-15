package com.grab.store.inventory;

import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryRootController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcSecurityTestConfiguration.class)
class InventoryRootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void root_shouldExposeCatalogProductLinksForCreateInventoryWorkflow() throws Exception {
        mockMvc.perform(get("/api/v1/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.search-products.href").value(containsString("/api/v1/catalog/products/search")))
                .andExpect(jsonPath("$._links.get-product.href").value(containsString("/api/v1/catalog/products/{productId}")))
                .andExpect(jsonPath("$._links.create-inventory-item.href").exists())
                .andExpect(jsonPath("$._links.search-inventory-items.href").exists());
    }
}
