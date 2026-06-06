package com.grab.store.inventory.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.inventory.internal.api.rest.assembler.BinModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.api.rest.service.BinCommandService;
import com.grab.store.inventory.internal.api.rest.service.BinQueryService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BinController.class)
class BinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BinCommandService binCommandService;

    @MockBean
    private BinQueryService binQueryService;

    @MockBean
    private BinModelAssembler binModelAssembler;

    private ObjectMapper objectMapper;

    private BinResponse sampleBinResponse;
    private CreateBinRequest sampleCreateRequest;
    private UpdateBinRequest sampleUpdateRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        sampleBinResponse = new BinResponse(
                "bin-1", "zone-1", "B-A1", "Bin A1", 100, true
        );

        sampleCreateRequest = new CreateBinRequest(
                "zone-1", "B-A1", "Bin A1", 100
        );

        sampleUpdateRequest = new UpdateBinRequest(
                "B-A1", "Bin A1 Updated", 200, true
        );

        when(binModelAssembler.toModel(any(BinResponse.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));
    }

    @Test
    void createBin_shouldReturn201() throws Exception {
        when(binCommandService.createBin(any(CreateBinRequest.class), eq("actor-1")))
                .thenReturn(sampleBinResponse);

        mockMvc.perform(post("/api/v1/bins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("bin-1"))
                .andExpect(jsonPath("$.code").value("B-A1"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createBin_withoutActorId_shouldReturn201() throws Exception {
        when(binCommandService.createBin(any(CreateBinRequest.class), eq(null)))
                .thenReturn(sampleBinResponse);

        mockMvc.perform(post("/api/v1/bins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("bin-1"));
    }

    @Test
    void createBin_withInvalidRequest_shouldReturn400() throws Exception {
        CreateBinRequest invalid = new CreateBinRequest(
                null, "", null, null
        );

        mockMvc.perform(post("/api/v1/bins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBin_shouldReturn200() throws Exception {
        BinResponse updated = new BinResponse(
                "bin-1", "zone-1", "B-A1", "Bin A1 Updated", 200, true
        );

        when(binCommandService.updateBin(eq("bin-1"), any(UpdateBinRequest.class), eq("actor-1")))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/bins/bin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Actor-Id", "actor-1")
                        .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("bin-1"))
                .andExpect(jsonPath("$.name").value("Bin A1 Updated"))
                .andExpect(jsonPath("$.maxCapacity").value(200));
    }

    @Test
    void activateBin_shouldReturn200() throws Exception {
        when(binCommandService.activateBin("bin-1", "actor-1"))
                .thenReturn(sampleBinResponse);

        mockMvc.perform(put("/api/v1/bins/bin-1/activate")
                        .header("X-Actor-Id", "actor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("bin-1"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void deactivateBin_shouldReturn200() throws Exception {
        BinResponse deactivated = new BinResponse(
                "bin-1", "zone-1", "B-A1", "Bin A1", 100, false
        );

        when(binCommandService.deactivateBin("bin-1", "actor-1"))
                .thenReturn(deactivated);

        mockMvc.perform(put("/api/v1/bins/bin-1/deactivate")
                        .header("X-Actor-Id", "actor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("bin-1"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void listBins_shouldReturn200() throws Exception {
        Page<BinResponse> page = new PageImpl<>(List.of(sampleBinResponse));
        when(binQueryService.listBins(eq("zone-1"), eq(true), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/bins/zones/zone-1")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.binResponseList").isArray())
                .andExpect(jsonPath("$._embedded.binResponseList[0].id").value("bin-1"))
                .andExpect(jsonPath("$._embedded.binResponseList[0].code").value("B-A1"));
    }

    @Test
    void listBins_withoutActive_shouldReturn200() throws Exception {
        Page<BinResponse> page = new PageImpl<>(List.of(sampleBinResponse));
        when(binQueryService.listBins(eq("zone-1"), eq(null), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/bins/zones/zone-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.binResponseList[0].id").value("bin-1"));
    }
}
