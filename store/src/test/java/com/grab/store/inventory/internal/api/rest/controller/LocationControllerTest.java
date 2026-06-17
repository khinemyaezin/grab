package com.grab.store.inventory.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grab.store.inventory.internal.api.rest.assembler.LocationModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.AddressRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationAddressResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.api.rest.service.LocationCommandService;
import com.grab.store.inventory.internal.api.rest.service.LocationQueryService;
import com.inventory.domain.enums.LocationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationCommandService locationCommandService;

    @MockBean
    private LocationQueryService locationQueryService;

    @MockBean
    private LocationModelAssembler locationModelAssembler;

    private ObjectMapper objectMapper;

    private LocationResponse sampleLocationResponse;
    private CreateLocationRequest sampleCreateRequest;
    private UpdateLocationRequest sampleUpdateRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleLocationResponse = new LocationResponse(
                "loc-1", "WH-001", "Warehouse 1", "WAREHOUSE", true,
                new LocationAddressResponse("123 Main St", null, "Springfield",
                        "IL", "62701", "USA")
        );

        sampleCreateRequest = new CreateLocationRequest(
                "WH-001", "Warehouse 1", LocationType.WAREHOUSE,
                new AddressRequest("123 Main St", null, "Springfield",
                        "IL", "62701", "USA")
        );

        sampleUpdateRequest = new UpdateLocationRequest(
                "WH-001", "Warehouse 1 Updated", LocationType.WAREHOUSE,
                new AddressRequest("456 Oak Ave", "Suite 100", "Chicago",
                        "IL", "60601", "USA")
        );

        when(locationModelAssembler.toModel(any(LocationResponse.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));
    }

    @Test
    void createLocation_shouldReturn201() throws Exception {
        when(locationCommandService.createLocation(any(CreateLocationRequest.class), eq("actor-1")))
                .thenReturn(sampleLocationResponse);

        mockMvc.perform(post("/api/v1/inventory/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("loc-1"))
                .andExpect(jsonPath("$.code").value("WH-001"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createLocation_withoutActorId_shouldReturn400() throws Exception {
        when(locationCommandService.createLocation(any(CreateLocationRequest.class), eq(null)))
                .thenReturn(sampleLocationResponse);

        mockMvc.perform(post("/api/v1/inventory/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLocation_withInvalidRequest_shouldReturn400() throws Exception {
        CreateLocationRequest invalid = new CreateLocationRequest(
                "", "", null, null
        );

        mockMvc.perform(post("/api/v1/inventory/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateLocation_shouldReturn200() throws Exception {
        LocationResponse updated = new LocationResponse(
                "loc-1", "WH-001", "Warehouse 1 Updated", "WAREHOUSE", true,
                new LocationAddressResponse("456 Oak Ave", "Suite 100", "Chicago",
                        "IL", "60601", "USA")
        );

        when(locationCommandService.updateLocation(eq("loc-1"), any(UpdateLocationRequest.class), eq("actor-1")))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/inventory/locations/loc-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("loc-1"))
                .andExpect(jsonPath("$.name").value("Warehouse 1 Updated"));
    }

    @Test
    void activateLocation_shouldReturn200() throws Exception {
        when(locationCommandService.activateLocation("loc-1", "actor-1"))
                .thenReturn(sampleLocationResponse);

        mockMvc.perform(patch("/api/v1/inventory/locations/loc-1/activate")
                        .header("X-Actor-Id", "actor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("loc-1"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void deactivateLocation_shouldReturn200() throws Exception {
        LocationResponse deactivated = new LocationResponse(
                "loc-1", "WH-001", "Warehouse 1", "WAREHOUSE", false,
                new LocationAddressResponse("123 Main St", null, "Springfield",
                        "IL", "62701", "USA")
        );

        when(locationCommandService.deactivateLocation("loc-1", "actor-1"))
                .thenReturn(deactivated);

        mockMvc.perform(patch("/api/v1/inventory/locations/loc-1/deactivate")
                        .header("X-Actor-Id", "actor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("loc-1"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void getLocation_shouldReturn200() throws Exception {
        when(locationQueryService.getLocation("loc-1"))
                .thenReturn(sampleLocationResponse);

        mockMvc.perform(get("/api/v1/inventory/locations/loc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("loc-1"))
                .andExpect(jsonPath("$.code").value("WH-001"));
    }

    @Test
    void getLocationByCode_shouldReturn200() throws Exception {
        when(locationQueryService.getLocationByCode("WH-001"))
                .thenReturn(sampleLocationResponse);

        mockMvc.perform(get("/api/v1/inventory/locations/code/WH-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("loc-1"))
                .andExpect(jsonPath("$.code").value("WH-001"));
    }

    @Test
    void listLocations_shouldReturn200() throws Exception {
        Page<LocationResponse> page = new PageImpl<>(List.of(sampleLocationResponse));
        when(locationQueryService.listLocations(
                eq("seller-1"), eq(true),
                eq(LocationType.WAREHOUSE),
                any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/inventory/locations")
                        .header("X-Actor-Id", "seller-1")
                        .param("active", "true")
                        .param("type", "WAREHOUSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.locationResponseList").isArray())
                .andExpect(jsonPath("$._embedded.locationResponseList[0].id").value("loc-1"))
                .andExpect(jsonPath("$._embedded.locationResponseList[0].code").value("WH-001"));
    }

    @Test
    void listLocations_withoutOptionalParams_shouldReturn200() throws Exception {
        Page<LocationResponse> page = new PageImpl<>(List.of(sampleLocationResponse));
        when(locationQueryService.listLocations(eq("seller-1"), eq(null), eq(null), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/inventory/locations")
                .header("X-Actor-Id", "seller-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.locationResponseList[0].id").value("loc-1"));
    }

    @Test
    void listLocations_withoutSellerId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/locations"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLocation_shouldReturn204() throws Exception {
        doNothing().when(locationCommandService).deleteLocation("loc-1", "actor-1");

        mockMvc.perform(delete("/api/v1/inventory/locations/loc-1")
                        .header("X-Actor-Id", "actor-1"))
                .andExpect(status().isNoContent());
    }
}
