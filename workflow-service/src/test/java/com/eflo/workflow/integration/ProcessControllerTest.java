package com.eflo.workflow.integration;

import com.eflo.workflow.domain.model.request.CreateProcessRequest;
import com.eflo.workflow.domain.model.response.ProcessResponse;
import com.eflo.workflow.service.ProcessDefinitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProcessController
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProcessDefinitionService processDefinitionService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProcessSuccessfully() throws Exception {
        // Given
        CreateProcessRequest request = CreateProcessRequest.builder()
                .processCode("VN_TEST")
                .processName("Test Workflow")
                .orderType("VN")
                .maxDurationDays(30)
                .build();

        ProcessResponse response = ProcessResponse.builder()
                .id(1L)
                .processCode("VN_TEST")
                .processName("Test Workflow")
                .orderType("VN")
                .isActive(true)
                .build();

        when(processDefinitionService.createProcess(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/workflow/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.processCode").value("VN_TEST"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetProcessById() throws Exception {
        // Given
        ProcessResponse response = ProcessResponse.builder()
                .id(1L)
                .processCode("VN_TEST")
                .processName("Test Workflow")
                .build();

        when(processDefinitionService.getProcessById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/workflow/processes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.processCode").value("VN_TEST"));
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/workflow/processes/1"))
                .andExpect(status().isUnauthorized());
    }
}
