package com.grab.store.merchant.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.merchant.internal.api.rest.assembler.MerchantMemberModelAssembler;
import com.grab.store.merchant.internal.api.rest.dto.request.ChangeMerchantMemberRoleRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.InviteMerchantMemberRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.grab.store.merchant.internal.api.rest.service.MerchantMemberCommandService;
import com.grab.store.merchant.internal.api.rest.service.MerchantMemberQueryService;
import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcSecurityTestConfiguration.class)
@TestPropertySource(properties = "merchant.enabled=true")
class MerchantMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MerchantMemberCommandService commands;

    @MockBean
    private MerchantMemberQueryService queries;

    @MockBean
    private MerchantMemberModelAssembler assembler;

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        when(assembler.toModel(any(MerchantMemberResponse.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));
        when(assembler.toCollectionModel(any(), any()))
                .thenAnswer(invocation -> CollectionModel.of(
                        ((List<MerchantMemberResponse>) invocation.getArgument(0)).stream()
                                .map(EntityModel::of)
                                .toList()
                ));
    }

    @Test
    void invite_shouldReturnCreated() throws Exception {
        MerchantMemberResponse response = sampleResponse("mem-1", "OPERATOR", "INVITED");
        when(commands.invite(eq("mer-1"), any(InviteMerchantMemberRequest.class), any())).thenReturn(response);

        InviteMerchantMemberRequest request =
                new InviteMerchantMemberRequest("usr-2", "OPERATOR", now.plusSeconds(86400));

        mockMvc.perform(post("/api/v1/merchants/mer-1/members/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value("mem-1"))
                .andExpect(jsonPath("$.role").value("OPERATOR"))
                .andExpect(jsonPath("$.status").value("INVITED"));
    }

    @Test
    void accept_shouldReturnOk() throws Exception {
        MerchantMemberResponse response = sampleResponse("mem-1", "OPERATOR", "ACTIVE");
        when(commands.accept(eq("mer-1"), eq("mem-1"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/merchants/mer-1/members/mem-1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void changeRole_shouldReturnOk() throws Exception {
        MerchantMemberResponse response = sampleResponse("mem-1", "ANALYST", "ACTIVE");
        when(commands.changeRole(eq("mer-1"), eq("mem-1"), any(ChangeMerchantMemberRoleRequest.class), any()))
                .thenReturn(response);

        ChangeMerchantMemberRoleRequest request = new ChangeMerchantMemberRoleRequest("ANALYST");

        mockMvc.perform(put("/api/v1/merchants/mer-1/members/mem-1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ANALYST"));
    }

    @Test
    void remove_shouldReturnOk() throws Exception {
        MerchantMemberResponse response = sampleResponse("mem-1", "OPERATOR", "REMOVED");
        when(commands.remove(eq("mer-1"), eq("mem-1"), any())).thenReturn(response);

        mockMvc.perform(delete("/api/v1/merchants/mer-1/members/mem-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REMOVED"));
    }

    @Test
    void list_shouldReturnCollectionModel() throws Exception {
        when(queries.list("mer-1")).thenReturn(List.of(
                sampleResponse("mem-1", "MERCHANT_ADMIN", "ACTIVE"),
                sampleResponse("mem-2", "OPERATOR", "INVITED")
        ));

        mockMvc.perform(get("/api/v1/merchants/mer-1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists());
    }

    private MerchantMemberResponse sampleResponse(String memberId, String role, String status) {
        return new MerchantMemberResponse(
                memberId,
                "mer-1",
                "usr-1",
                role,
                status,
                null,
                null,
                now,
                now,
                now,
                0L
        );
    }
}
