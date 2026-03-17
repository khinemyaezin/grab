package com.grab.store.inventory.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.AddBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.AddZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.AddressRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.*;
import com.grab.store.inventory.internal.api.rest.service.LocationFacadeService;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.enums.ZoneType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
class LocationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LocationFacadeService locationFacadeService;

    @Test
    void createLocation_returnsCreated() throws Exception {
        CreateLocationRequest request = new CreateLocationRequest(
                "WH-01",
                "Warehouse 01",
                LocationType.WAREHOUSE,
                new AddressRequest("Line 1", null, "Bangkok", null, "10110", "TH")
        );

        LocationResponse response = location("loc-1", "WH-01", "Warehouse 01", List.of());

        given(locationFacadeService.createLocation(eq(request), eq("actor-1")))
                .willReturn(EntityModel.of(response));

        MvcResult result = mockMvc.perform(post("/api/v1/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        LocationResponse payload = readLocation(result);
        assertThat(payload.id()).isEqualTo("loc-1");
        assertThat(payload.code()).isEqualTo("WH-01");
        assertThat(payload.type()).isEqualTo("WAREHOUSE");
    }

    @Test
    void getLocation_returnsLocation() throws Exception {
        LocationResponse response = location("loc-1", "WH-01", "Warehouse 01", List.of());
        given(locationFacadeService.getLocation("loc-1")).willReturn(EntityModel.of(response));

        MvcResult result = mockMvc.perform(get("/api/v1/locations/{locationId}", "loc-1"))
                .andExpect(status().isOk())
                .andReturn();

        LocationResponse payload = readLocation(result);
        assertThat(payload.id()).isEqualTo("loc-1");
        assertThat(payload.name()).isEqualTo("Warehouse 01");
    }

    @Test
    void listLocations_withFilters_returnsItems() throws Exception {
        LocationsResponse response = new LocationsResponse(List.of(
                location("loc-1", "WH-01", "Warehouse 01", List.of()),
                location("loc-2", "WH-02", "Warehouse 02", List.of())
        ));

        given(locationFacadeService.listLocations(true, LocationType.WAREHOUSE))
                .willReturn(EntityModel.of(response));

        MvcResult result = mockMvc.perform(get("/api/v1/locations")
                        .param("active", "true")
                        .param("type", "WAREHOUSE"))
                .andExpect(status().isOk())
                .andReturn();

        LocationsResponse payload = readLocations(result);
        assertThat(payload.items()).hasSize(2);
        assertThat(payload.items()).extracting(LocationResponse::code)
                .containsExactly("WH-01", "WH-02");
    }

    @Test
    void addZoneAndBinEndpoints_returnUpdatedLocation() throws Exception {
        AddZoneRequest addZoneRequest = new AddZoneRequest("PICK-1", "Picking Zone", ZoneType.PICKING, true);
        AddBinRequest addBinRequest = new AddBinRequest("BIN-1", "Primary Bin", 100, true);

        ZoneResponse zone = new ZoneResponse(
                "zone-1",
                "PICK-1",
                "Picking Zone",
                "PICKING",
                true,
                List.of(new BinResponse("bin-1", "BIN-1", "Primary Bin", 100, true))
        );

        LocationResponse updated = location("loc-1", "WH-01", "Warehouse 01", List.of(zone));

        given(locationFacadeService.addZone("loc-1", addZoneRequest, "actor-1"))
                .willReturn(EntityModel.of(updated));
        given(locationFacadeService.addBin("loc-1", "zone-1", addBinRequest, "actor-1"))
                .willReturn(EntityModel.of(updated));

        MvcResult zoneResult = mockMvc.perform(post("/api/v1/locations/{locationId}/zones", "loc-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(addZoneRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LocationResponse zonePayload = readLocation(zoneResult);
        assertThat(zonePayload.zones()).hasSize(1);
        assertThat(zonePayload.zones().getFirst().code()).isEqualTo("PICK-1");

        MvcResult binResult = mockMvc.perform(post("/api/v1/locations/{locationId}/zones/{zoneId}/bins", "loc-1", "zone-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(addBinRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LocationResponse binPayload = readLocation(binResult);
        assertThat(binPayload.zones().getFirst().bins()).hasSize(1);
        assertThat(binPayload.zones().getFirst().bins().getFirst().code()).isEqualTo("BIN-1");
    }

    private LocationResponse location(String id, String code, String name, List<ZoneResponse> zones) {
        return new LocationResponse(
                id,
                code,
                name,
                "WAREHOUSE",
                true,
                new LocationAddressResponse("Line 1", null, "Bangkok", null, "10110", "TH"),
                zones
        );
    }

    private LocationResponse readLocation(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), LocationResponse.class);
    }

    private LocationsResponse readLocations(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), LocationsResponse.class);
    }
}
