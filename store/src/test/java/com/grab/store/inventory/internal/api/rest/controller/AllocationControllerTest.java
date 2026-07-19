package com.grab.store.inventory.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.inventory.internal.api.rest.assembler.AllocationModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.AllocateStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.DeallocateStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.AllocateStockResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.AllocationAvailabilityResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.DeallocateStockResponse;
import com.grab.store.inventory.internal.api.rest.service.AuthenticatedInventoryScopeResolver;
import com.grab.store.inventory.internal.api.rest.service.InventoryCommandService;
import com.grab.store.inventory.internal.api.rest.service.InventoryQueryService;
import com.grab.store.inventory.internal.api.rest.service.ResolvedInventoryAccess;
import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AllocationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcSecurityTestConfiguration.class)
class AllocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryCommandService inventoryCommandService;

    @MockBean
    private InventoryQueryService inventoryQueryService;

    @MockBean
    private AllocationModelAssembler allocationModelAssembler;

    @MockBean
    private AuthenticatedInventoryScopeResolver scopeResolver;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        when(scopeResolver.resolve(any())).thenReturn(new ResolvedInventoryAccess("actor-1", "merchant", "m-1"));
    }

    @Test
    void allocate_shouldReturnCreated() throws Exception {
        AllocateStockResponse response = new AllocateStockResponse(
                true, "SKU-1", 2, 2, "ord-1",
                List.of(new AllocateStockResponse.AllocationLineResponse("r1", "i1", "l1", 2))
        );
        when(inventoryCommandService.allocateStock(any(AllocateStockRequest.class), any()))
                .thenReturn(response);
        when(allocationModelAssembler.toAllocateModel(response)).thenReturn(EntityModel.of(response));

        mockMvc.perform(post("/api/v1/inventory/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AllocateStockRequest(
                                "SKU-1", 2, "ord-1", "line-1", null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.allocatedQuantity").value(2));
    }

    @Test
    void deallocate_shouldReturnOk() throws Exception {
        DeallocateStockResponse response = new DeallocateStockResponse("SKU-1", "ord-1", 2, 2);
        when(inventoryCommandService.deallocateStock(any(DeallocateStockRequest.class), any()))
                .thenReturn(response);
        when(allocationModelAssembler.toDeallocateModel(response)).thenReturn(EntityModel.of(response));

        mockMvc.perform(post("/api/v1/inventory/allocations/deallocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeallocateStockRequest("SKU-1", 2, "ord-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releasedQuantity").value(2));
    }

    @Test
    void availability_shouldReturnOk() throws Exception {
        AllocationAvailabilityResponse response = new AllocationAvailabilityResponse("SKU-1", 10, true, 2);
        when(inventoryQueryService.getAllocationAvailability(eq("SKU-1"), eq(2))).thenReturn(response);
        when(allocationModelAssembler.toAvailabilityModel(response)).thenReturn(EntityModel.of(response));

        mockMvc.perform(get("/api/v1/inventory/allocations/availability")
                        .param("sku", "SKU-1")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canAllocate").value(true))
                .andExpect(jsonPath("$.availableQuantity").value(10));
    }

    @Test
    void availability_withoutQuantity_shouldPassNull() throws Exception {
        AllocationAvailabilityResponse response = new AllocationAvailabilityResponse("SKU-1", 5, true, 0);
        when(inventoryQueryService.getAllocationAvailability(eq("SKU-1"), nullable(Integer.class))).thenReturn(response);
        when(allocationModelAssembler.toAvailabilityModel(response)).thenReturn(EntityModel.of(response));

        mockMvc.perform(get("/api/v1/inventory/allocations/availability").param("sku", "SKU-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(5));
    }
}
