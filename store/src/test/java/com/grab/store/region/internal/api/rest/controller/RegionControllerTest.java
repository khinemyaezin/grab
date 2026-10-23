package com.grab.store.region.internal.api.rest.controller;

import com.grab.store.region.internal.api.rest.assembler.RegionModelAssembler;
import com.grab.store.region.internal.api.rest.dto.response.RegionCountryResponse;
import com.grab.store.region.internal.api.rest.dto.response.RegionResponse;
import com.grab.store.region.internal.api.rest.service.RegionCountryQueryService;
import com.grab.store.region.internal.api.rest.service.RegionQueryService;
import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import com.region.application.exception.RegionServiceError;
import com.region.application.exception.RegionServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({WebMvcSecurityTestConfiguration.class, RegionModelAssembler.class})
class RegionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegionQueryService regionQueryService;

    @MockBean
    private RegionCountryQueryService regionCountryQueryService;

    @Test
    void get_whenRegionExists_shouldReturnEntityModelWithLinks() throws Exception {
        RegionResponse response = new RegionResponse(
                "reg-1",
                "Southeast Asia",
                "SGD",
                "ACTIVE",
                Set.of("SG", "MY")
        );

        when(regionQueryService.get("reg-1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/regions/reg-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regionId").value("reg-1"))
                .andExpect(jsonPath("$.name").value("Southeast Asia"))
                .andExpect(jsonPath("$.currencyCode").value("SGD"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.get-region.href").exists());
    }

    @Test
    void get_whenRegionNotFound_shouldReturn404ProblemDetail() throws Exception {
        when(regionQueryService.get("unknown"))
                .thenThrow(new RegionServiceException(
                        new RegionServiceError.RegionNotFound("unknown"),
                        "Region not found"
                ));

        mockMvc.perform(get("/api/v1/regions/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void countries_whenCountriesExist_shouldReturnCollectionModel() throws Exception {
        when(regionCountryQueryService.list()).thenReturn(List.of(
                new RegionCountryResponse("SG"),
                new RegionCountryResponse("MY")
        ));

        mockMvc.perform(get("/api/v1/regions/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.regionCountryResponseList[0].countryCode").value("SG"))
                .andExpect(jsonPath("$._embedded.regionCountryResponseList[1].countryCode").value("MY"));
    }
}
