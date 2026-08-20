package com.grab.store.inventory.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grab.store.inventory.internal.api.rest.assembler.CheckInventoryExistenceModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryMovementModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryReservationModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventorySummaryModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.AdjustStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CheckInventoryExistenceRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReserveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.CheckInventoryExistenceResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.StockMovementResponse;
import com.grab.store.inventory.internal.api.rest.service.AuthenticatedInventoryScopeResolver;
import com.grab.store.inventory.internal.api.rest.service.ResolvedInventoryAccess;
import com.grab.store.inventory.internal.api.rest.service.InventoryCommandService;
import com.grab.store.inventory.internal.api.rest.service.InventoryQueryService;
import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.inventory.domain.enums.AdjustmentReason;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.enums.StockMovementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcSecurityTestConfiguration.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryCommandService inventoryCommandService;

    @MockBean
    private InventoryQueryService inventoryQueryService;

    @MockBean
    private InventoryModelAssembler inventoryModelAssembler;

    @MockBean
    private InventoryMovementModelAssembler inventoryMovementModelAssembler;

    @MockBean
    private InventoryReservationModelAssembler inventoryReservationModelAssembler;

    @MockBean
    private CheckInventoryExistenceModelAssembler checkInventoryExistenceModelAssembler;

    @MockBean
    private InventorySummaryModelAssembler inventorySummaryModelAssembler;

    @MockBean
    private AuthenticatedInventoryScopeResolver scopeResolver;

    private ObjectMapper objectMapper;

    private InventoryResponse sampleInventoryResponse;
    private InventoryReservationResponse sampleReservationResponse;
    private StockMovementResponse sampleMovementResponse;
    private CreateInventoryRequest sampleCreateRequest;
    private ReceiveStockRequest sampleReceiveRequest;
    private ReserveStockRequest sampleReserveRequest;
    private AdjustStockRequest sampleAdjustRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleInventoryResponse = new InventoryResponse(
                "inv-1", "SKU-001", "seller-1", "variant-1", "T-Shirt",
                "loc-1", "LOC-1", "Warehouse One", 100, 10, 0, 0, 90,
                "ACTIVE", 20, 30, 50, 200
        );

        sampleReservationResponse = new InventoryReservationResponse(
                "res-1", "inv-1", "order-1",
                "line-1", 5, "ACTIVE",
                LocalDateTime.now().plusDays(1), "idem-1"
        );

        sampleMovementResponse = new StockMovementResponse(
                "mov-1", "inv-1", "RECEIVE", 50,
                100, 150, 80, 130, 10, 10,
                "ref-1", LocalDateTime.now()
        );

        sampleCreateRequest = new CreateInventoryRequest(
                "SKU-001",
                "loc-1", 100, 20, 30, 50, 200
        );

        sampleReceiveRequest = new ReceiveStockRequest(
                50, StockMovementType.SALE, "PO-001"
        );

        sampleReserveRequest = new ReserveStockRequest(
                5, "order-1", "line-1", LocalDateTime.now().plusDays(1)
        );

        sampleAdjustRequest = new AdjustStockRequest(
                80, AdjustmentReason.DAMAGED
        );

        when(inventoryModelAssembler.toModel(any(InventoryResponse.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));

        when(inventoryReservationModelAssembler.toModel(any(InventoryReservationResponse.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));

        when(inventoryMovementModelAssembler.toModel(any(StockMovementResponse.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));

        when(scopeResolver.resolveOwnerMerchantId(any())).thenReturn("actor-1");
        when(scopeResolver.resolve(any())).thenReturn(new ResolvedInventoryAccess("actor-1", "merchant-account", "merchant-123"));

    }

    @Test
    void createInventory_shouldReturn201() throws Exception {
        when(inventoryCommandService.createInventory(any(CreateInventoryRequest.class), nullable(String.class), any(ResolvedInventoryAccess.class)))
                .thenReturn(sampleInventoryResponse);

        mockMvc.perform(post("/api/v1/inventory/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("inv-1"))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.onHand").value(100));
    }

    @Test
    void createInventory_withoutActorId_shouldReturn201() throws Exception {
        when(scopeResolver.resolveOwnerMerchantId(any())).thenReturn(null);
        when(inventoryCommandService.createInventory(any(CreateInventoryRequest.class), nullable(String.class), any(ResolvedInventoryAccess.class)))
                .thenReturn(sampleInventoryResponse);

        mockMvc.perform(post("/api/v1/inventory/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("inv-1"));
    }

    @Test
    void getInventory_shouldReturn200() throws Exception {
        when(inventoryQueryService.getInventory("inv-1"))
                .thenReturn(sampleInventoryResponse);

        mockMvc.perform(get("/api/v1/inventory/items/inv-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("inv-1"))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.locationName").value("Warehouse One"));
    }

    @Test
    void receiveStock_shouldReturn200() throws Exception {
        when(inventoryCommandService.receiveStock(eq("inv-1"), any(ReceiveStockRequest.class), any(ResolvedInventoryAccess.class)))
                .thenReturn(sampleInventoryResponse);

        mockMvc.perform(post("/api/v1/inventory/items/inv-1/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(sampleReceiveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("inv-1"))
                .andExpect(jsonPath("$.onHand").value(100));
    }

    @Test
    void reserveStock_shouldReturn200() throws Exception {
        when(inventoryCommandService.reserveStock(
                eq("inv-1"), any(ReserveStockRequest.class), eq("idem-1"), any(ResolvedInventoryAccess.class)))
                .thenReturn(sampleReservationResponse);

        mockMvc.perform(post("/api/v1/inventory/items/inv-1/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .header("Idempotency-Key", "idem-1")
                        .content(objectMapper.writeValueAsString(sampleReserveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("res-1"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void reserveStock_withoutIdempotencyKey_shouldReturn200() throws Exception {
        when(inventoryCommandService.reserveStock(
                eq("inv-1"), any(ReserveStockRequest.class), eq(null), any(ResolvedInventoryAccess.class)))
                .thenReturn(sampleReservationResponse);

        mockMvc.perform(post("/api/v1/inventory/items/inv-1/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(sampleReserveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("res-1"));
    }

    @Test
    void releaseReservation_shouldReturn200() throws Exception {
        when(inventoryCommandService.releaseReservation("inv-1", "res-1", new ResolvedInventoryAccess("actor-1", "merchant-account", "merchant-123")))
                .thenReturn(sampleReservationResponse);

        mockMvc.perform(post("/api/v1/inventory/items/inv-1/reservations/res-1/release")
                        .header("X-Actor-Id", "actor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("res-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shipReservation_shouldReturn200() throws Exception {
        when(inventoryCommandService.shipReservation("inv-1", "res-1", new ResolvedInventoryAccess("actor-1", "merchant-account", "merchant-123")))
                .thenReturn(sampleReservationResponse);

        mockMvc.perform(post("/api/v1/inventory/items/inv-1/reservations/res-1/ship")
                        .header("X-Actor-Id", "actor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("res-1"));
    }

    @Test
    void adjustStock_shouldReturn200() throws Exception {
        when(inventoryCommandService.adjustStock(eq("inv-1"), any(AdjustStockRequest.class), any(ResolvedInventoryAccess.class)))
                .thenReturn(sampleInventoryResponse);

        mockMvc.perform(post("/api/v1/inventory/items/inv-1/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(sampleAdjustRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("inv-1"))
                .andExpect(jsonPath("$.onHand").value(100));
    }

    @Test
    void getMovements_shouldReturn200() throws Exception {
        Page<StockMovementResponse> page = new PageImpl<>(List.of(sampleMovementResponse));
        when(inventoryQueryService.getMovements(eq("inv-1"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/inventory/items/inv-1/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.stockMovementResponseList").isArray())
                .andExpect(jsonPath("$._embedded.stockMovementResponseList[0].id").value("mov-1"))
                .andExpect(jsonPath("$._embedded.stockMovementResponseList[0].type").value("RECEIVE"));
    }

    @Test
    void getMovements_withPageable_shouldApplyDefaultPaging() throws Exception {
        Page<StockMovementResponse> page = new PageImpl<>(List.of(sampleMovementResponse));
        when(inventoryQueryService.getMovements(eq("inv-1"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/inventory/items/inv-1/movements")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.stockMovementResponseList[0].id").value("mov-1"));
    }

    @Test
    void getReservations_shouldReturn200() throws Exception {
        Page<InventoryReservationResponse> page = new PageImpl<>(List.of(sampleReservationResponse));
        when(inventoryQueryService.getReservations(eq("inv-1"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/inventory/items/inv-1/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.inventoryReservationResponseList").isArray())
                .andExpect(jsonPath("$._embedded.inventoryReservationResponseList[0].id").value("res-1"))
                .andExpect(jsonPath("$._embedded.inventoryReservationResponseList[0].quantity").value(5));
    }

    @Test
    void getReservations_withPageable_shouldApplyDefaultPaging() throws Exception {
        Page<InventoryReservationResponse> page = new PageImpl<>(List.of(sampleReservationResponse));
        when(inventoryQueryService.getReservations(eq("inv-1"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/inventory/items/inv-1/reservations")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.inventoryReservationResponseList[0].id").value("res-1"));
    }

    @Test
    void createInventory_withInvalidRequest_shouldReturn400() throws Exception {
        CreateInventoryRequest invalidRequest = new CreateInventoryRequest(
                "", "", -1, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/inventory/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void receiveStock_withInvalidQuantity_shouldReturn400() throws Exception {
        ReceiveStockRequest invalidRequest = new ReceiveStockRequest(
                0, StockMovementType.SALE, null
        );

        mockMvc.perform(post("/api/v1/inventory/items/inv-1/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchInventoryItems_shouldReturn200() throws Exception {
        when(scopeResolver.resolveOwnerMerchantId(any())).thenReturn("seller-1");
        Page<InventoryResponse> page = new PageImpl<>(List.of(sampleInventoryResponse));
        when(inventoryQueryService.searchInventoryItems(eq("seller-1"), any(SearchInventoryRequest.class), any(Pageable.class)))
                .thenReturn(page);

        SearchInventoryRequest request = new SearchInventoryRequest("SKU", "loc-1", InventoryStatus.ACTIVE, null);

        mockMvc.perform(post("/api/v1/inventory/items/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "seller-1")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.inventoryResponseList").isArray())
                .andExpect(jsonPath("$._embedded.inventoryResponseList[0].id").value("inv-1"))
                .andExpect(jsonPath("$._embedded.inventoryResponseList[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$._embedded.inventoryResponseList[0].productVariantId").value("variant-1"))
                .andExpect(jsonPath("$._embedded.inventoryResponseList[0].locationCode").value("LOC-1"))
                .andExpect(jsonPath("$._embedded.inventoryResponseList[0].locationName").value("Warehouse One"))
                .andExpect(jsonPath("$._links.search-inventory-items.href").exists())
                .andExpect(jsonPath("$._links.create-inventory-item.href").exists())
                .andExpect(jsonPath("$._links.check-inventory-items-existence.href")
                        .value(containsString("/api/v1/inventory/items/existence")))
                .andExpect(jsonPath("$._links.search-product-variants.href").value(containsString("/api/v1/catalog/products/variants/search")));
    }

    @Test
    void searchInventoryItems_withoutSellerId_shouldReturn403() throws Exception {
        when(scopeResolver.resolveOwnerMerchantId(any())).thenThrow(new InventoryServiceException(
                new InventoryServiceError.InventoryScopeForbidden("UNKNOWN", "UNKNOWN", "UNKNOWN")));

        SearchInventoryRequest request = new SearchInventoryRequest(null, null, null, null);

        mockMvc.perform(post("/api/v1/inventory/items/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkExistence_shouldReturn200() throws Exception {
        when(scopeResolver.resolveOwnerMerchantId(any())).thenReturn("seller-1");
        CheckInventoryExistenceResponse response = new CheckInventoryExistenceResponse(List.of(
                new CheckInventoryExistenceResponse.Entry("SKU-001", true, "inv-1"),
                new CheckInventoryExistenceResponse.Entry("SKU-002", false, null)
        ));
        when(inventoryQueryService.checkExistence(eq("seller-1"), any(CheckInventoryExistenceRequest.class)))
                .thenReturn(response);
        when(checkInventoryExistenceModelAssembler.toModel(response))
                .thenReturn(EntityModel.of(response));

        CheckInventoryExistenceRequest request = new CheckInventoryExistenceRequest(
                "loc-1",
                List.of("SKU-001", "SKU-002")
        );

        mockMvc.perform(post("/api/v1/inventory/items/existence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$.items[0].exists").value(true))
                .andExpect(jsonPath("$.items[0].inventoryItemId").value("inv-1"))
                .andExpect(jsonPath("$.items[1].sku").value("SKU-002"))
                .andExpect(jsonPath("$.items[1].exists").value(false));
    }

    @Test
    void checkExistence_withoutSellerId_shouldReturn403() throws Exception {
        when(scopeResolver.resolveOwnerMerchantId(any())).thenThrow(new InventoryServiceException(
                new InventoryServiceError.InventoryScopeForbidden("UNKNOWN", "UNKNOWN", "UNKNOWN")));

        CheckInventoryExistenceRequest request = new CheckInventoryExistenceRequest(
                "loc-1",
                List.of("SKU-001")
        );

        mockMvc.perform(post("/api/v1/inventory/items/existence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
