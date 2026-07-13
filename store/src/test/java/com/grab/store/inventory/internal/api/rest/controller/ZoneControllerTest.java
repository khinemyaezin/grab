package com.grab.store.inventory.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.inventory.internal.api.rest.assembler.ZoneModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.api.rest.service.AuthenticatedInventoryScopeResolver;
import com.grab.store.inventory.internal.api.rest.service.ResolvedInventoryAccess;
import com.grab.store.inventory.internal.api.rest.service.ZoneCommandService;
import com.grab.store.inventory.internal.api.rest.service.ZoneQueryService;
import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import com.inventory.domain.enums.ZoneType;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ZoneController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcSecurityTestConfiguration.class)
class ZoneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ZoneCommandService zoneCommandService;

    @MockBean
    private ZoneQueryService zoneQueryService;

    @MockBean
    private ZoneModelAssembler zoneModelAssembler;

    @MockBean
    private AuthenticatedInventoryScopeResolver scopeResolver;

    private ObjectMapper objectMapper;

    private ZoneResponse sampleZoneResponse;
    private CreateZoneRequest sampleCreateRequest;
    private UpdateZoneRequest sampleUpdateRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        sampleZoneResponse = new ZoneResponse(
                "zone-1", "loc-1", "Z-A1", "Zone A1", "STORAGE", true
        );

        sampleCreateRequest = new CreateZoneRequest(
                "Z-A1", "Zone A1", ZoneType.STORAGE
        );

        sampleUpdateRequest = new UpdateZoneRequest(
                "Z-A1", "Zone A1 Updated", ZoneType.STORAGE, true
        );

        when(zoneModelAssembler.toModel(any(ZoneResponse.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));

        when(scopeResolver.resolveOwnerMerchantId(any())).thenReturn("actor-1");
        when(scopeResolver.resolve(any())).thenReturn(new ResolvedInventoryAccess("actor-1", "merchant-account", "merchant-123"));

    }

    @Test
    void createZone_shouldReturn201() throws Exception {
        when(zoneCommandService.createZone(eq("loc-1"), any(CreateZoneRequest.class), any(ResolvedInventoryAccess.class)))
                .thenReturn(sampleZoneResponse);

        mockMvc.perform(post("/api/v1/inventory/zones/locations/loc-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("zone-1"))
                .andExpect(jsonPath("$.code").value("Z-A1"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createZone_withoutActorId_shouldReturn201() throws Exception {
        when(scopeResolver.resolveOwnerMerchantId(any())).thenReturn(null);
        when(zoneCommandService.createZone(eq("loc-1"), any(CreateZoneRequest.class), any(ResolvedInventoryAccess.class)))
                .thenReturn(sampleZoneResponse);

        mockMvc.perform(post("/api/v1/inventory/zones/locations/loc-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("zone-1"));
    }

    @Test
    void createZone_withInvalidRequest_shouldReturn400() throws Exception {
        CreateZoneRequest invalid = new CreateZoneRequest(
                "", "", null
        );

        mockMvc.perform(post("/api/v1/inventory/zones/locations/loc-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMetadataZone_shouldReturn200() throws Exception {
        ZoneResponse updated = new ZoneResponse(
                "zone-1", "loc-1", "Z-A1", "Zone A1 Updated", "STORAGE", true
        );

        when(zoneCommandService.updateZone(eq("zone-1"), any(UpdateZoneRequest.class), any(ResolvedInventoryAccess.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/inventory/zones/zone-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("zone-1"))
                .andExpect(jsonPath("$.name").value("Zone A1 Updated"));
    }

    @Test
    void activateZone_shouldReturn200() throws Exception {
        when(zoneCommandService.activateZone("zone-1", new ResolvedInventoryAccess("actor-1", "merchant-account", "merchant-123")))
                .thenReturn(sampleZoneResponse);

        mockMvc.perform(patch("/api/v1/inventory/zones/zone-1/activate")
                        .header("X-Actor-Id", "actor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("zone-1"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void deactivateZone_shouldReturn200() throws Exception {
        ZoneResponse deactivated = new ZoneResponse(
                "zone-1", "loc-1", "Z-A1", "Zone A1", "STORAGE", false
        );

        when(zoneCommandService.deactivateZone("zone-1", new ResolvedInventoryAccess("actor-1", "merchant-account", "merchant-123")))
                .thenReturn(deactivated);

        mockMvc.perform(patch("/api/v1/inventory/zones/zone-1/deactivate")
                        .header("X-Actor-Id", "actor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("zone-1"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void listZones_shouldReturn200() throws Exception {
        Page<ZoneResponse> page = new PageImpl<>(List.of(sampleZoneResponse));
        when(zoneQueryService.listZones(eq("loc-1"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/inventory/zones/locations/loc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.zoneResponseList").isArray())
                .andExpect(jsonPath("$._embedded.zoneResponseList[0].id").value("zone-1"))
                .andExpect(jsonPath("$._embedded.zoneResponseList[0].code").value("Z-A1"));
    }

    @Test
    void getZoneById_shouldReturn200() throws Exception {
        when(zoneQueryService.getZone("zone-1"))
                .thenReturn(sampleZoneResponse);

        mockMvc.perform(get("/api/v1/inventory/zones/zone-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("zone-1"))
                .andExpect(jsonPath("$.code").value("Z-A1"));
    }

    @Test
    void deleteZone_shouldReturn204() throws Exception {
        doNothing().when(zoneCommandService).deleteZone("zone-1", new ResolvedInventoryAccess("actor-1", "merchant-account", "merchant-123"));

        mockMvc.perform(delete("/api/v1/inventory/zones/zone-1")
                        .header("X-Actor-Id", "actor-1"))
                .andExpect(status().isNoContent());
    }
}
