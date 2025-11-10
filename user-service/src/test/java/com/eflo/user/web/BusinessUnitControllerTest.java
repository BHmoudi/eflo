package com.eflo.user.web;

import com.eflo.user.domain.dto.BusinessUnitDTO;
import com.eflo.user.domain.dto.CreateBusinessUnitRequest;
import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.enums.BusinessUnitType;
import com.eflo.user.mapper.BusinessUnitMapper;
import com.eflo.user.service.BusinessUnitService;
import com.eflo.user.util.JwtAuthenticationHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusinessUnitController.class)
class BusinessUnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessUnitService businessUnitService;

    @MockBean
    private BusinessUnitMapper businessUnitMapper;

    @MockBean
    private JwtAuthenticationHelper jwtHelper;

    private BusinessUnit testBusinessUnit;
    private BusinessUnitDTO testBusinessUnitDTO;

    @BeforeEach
    void setUp() {
        testBusinessUnit = new BusinessUnit();
        testBusinessUnit.setId(1L);
        testBusinessUnit.setCode("DEPT001");
        testBusinessUnit.setName("Test Department");
        testBusinessUnit.setType(BusinessUnitType.DEPARTMENT);
        testBusinessUnit.setRegionCode("US-EAST");
        testBusinessUnit.setActive(true);

        testBusinessUnitDTO = BusinessUnitDTO.builder()
                .id(1L)
                .code("DEPT001")
                .name("Test Department")
                .type(BusinessUnitType.DEPARTMENT)
                .regionCode("US-EAST")
                .isActive(true)
                .build();

        when(jwtHelper.getCurrentUsername()).thenReturn("admin");
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void createBusinessUnit_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // Given
        CreateBusinessUnitRequest request = new CreateBusinessUnitRequest();
        request.setCode("DEPT002");
        request.setName("New Department");
        request.setType(BusinessUnitType.DEPARTMENT);

        when(businessUnitMapper.toEntity(any(CreateBusinessUnitRequest.class))).thenReturn(testBusinessUnit);
        when(businessUnitService.createBusinessUnit(any(BusinessUnit.class), anyString())).thenReturn(testBusinessUnit);
        when(businessUnitMapper.toDTO(any(BusinessUnit.class))).thenReturn(testBusinessUnitDTO);

        // When/Then
        mockMvc.perform(post("/api/v1/business-units")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("DEPT001"))
                .andExpect(jsonPath("$.name").value("Test Department"));
    }

    @Test
    @WithMockUser
    void getBusinessUnitById_ShouldReturnBusinessUnit_WhenExists() throws Exception {
        // Given
        when(businessUnitService.findById(1L)).thenReturn(Optional.of(testBusinessUnit));
        when(businessUnitMapper.toDTO(any(BusinessUnit.class))).thenReturn(testBusinessUnitDTO);

        // When/Then
        mockMvc.perform(get("/api/v1/business-units/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("DEPT001"));
    }

    @Test
    @WithMockUser
    void getBusinessUnitById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Given
        when(businessUnitService.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/business-units/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getBusinessUnitByCode_ShouldReturnBusinessUnit_WhenExists() throws Exception {
        // Given
        when(businessUnitService.findByCode("DEPT001")).thenReturn(Optional.of(testBusinessUnit));
        when(businessUnitMapper.toDTO(any(BusinessUnit.class))).thenReturn(testBusinessUnitDTO);

        // When/Then
        mockMvc.perform(get("/api/v1/business-units/code/DEPT001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DEPT001"));
    }

    @Test
    @WithMockUser
    void getAllBusinessUnits_ShouldReturnPagedResponse() throws Exception {
        // Given
        Page<BusinessUnit> buPage = new PageImpl<>(Arrays.asList(testBusinessUnit), PageRequest.of(0, 10), 1);
        when(businessUnitService.findAll(any(PageRequest.class))).thenReturn(buPage);
        when(businessUnitMapper.toDTOList(anyList())).thenReturn(Arrays.asList(testBusinessUnitDTO));

        // When/Then
        mockMvc.perform(get("/api/v1/business-units?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessUnits").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void getActiveBusinessUnits_ShouldReturnActiveUnits() throws Exception {
        // Given
        List<BusinessUnit> activeUnits = Arrays.asList(testBusinessUnit);
        when(businessUnitService.findAllActive()).thenReturn(activeUnits);
        when(businessUnitMapper.toDTOList(anyList())).thenReturn(Arrays.asList(testBusinessUnitDTO));

        // When/Then
        mockMvc.perform(get("/api/v1/business-units/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].code").value("DEPT001"));
    }

    @Test
    @WithMockUser
    void getBusinessUnitsByType_ShouldReturnUnits() throws Exception {
        // Given
        List<BusinessUnit> units = Arrays.asList(testBusinessUnit);
        when(businessUnitService.findByType(BusinessUnitType.DEPARTMENT)).thenReturn(units);
        when(businessUnitMapper.toDTOList(anyList())).thenReturn(Arrays.asList(testBusinessUnitDTO));

        // When/Then
        mockMvc.perform(get("/api/v1/business-units/type/DEPARTMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").value("DEPARTMENT"));
    }

    @Test
    @WithMockUser
    void getBusinessUnitsByRegion_ShouldReturnUnits() throws Exception {
        // Given
        List<BusinessUnit> units = Arrays.asList(testBusinessUnit);
        when(businessUnitService.findByRegionCode("US-EAST")).thenReturn(units);
        when(businessUnitMapper.toDTOList(anyList())).thenReturn(Arrays.asList(testBusinessUnitDTO));

        // When/Then
        mockMvc.perform(get("/api/v1/business-units/region/US-EAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].regionCode").value("US-EAST"));
    }

    @Test
    @WithMockUser
    void searchBusinessUnits_ShouldReturnMatchingUnits() throws Exception {
        // Given
        List<BusinessUnit> results = Arrays.asList(testBusinessUnit);
        when(businessUnitService.searchBusinessUnits("test")).thenReturn(results);
        when(businessUnitMapper.toDTOList(anyList())).thenReturn(Arrays.asList(testBusinessUnitDTO));

        // When/Then
        mockMvc.perform(get("/api/v1/business-units/search?q=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void updateBusinessUnit_ShouldReturnUpdated() throws Exception {
        // Given
        CreateBusinessUnitRequest request = new CreateBusinessUnitRequest();
        request.setName("Updated Department");

        when(businessUnitMapper.toEntity(any(CreateBusinessUnitRequest.class))).thenReturn(testBusinessUnit);
        when(businessUnitService.updateBusinessUnit(anyLong(), any(BusinessUnit.class), anyString()))
                .thenReturn(testBusinessUnit);
        when(businessUnitMapper.toDTO(any(BusinessUnit.class))).thenReturn(testBusinessUnitDTO);

        // When/Then
        mockMvc.perform(put("/api/v1/business-units/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void deactivateBusinessUnit_ShouldReturnOk() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/business-units/1/deactivate")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void reactivateBusinessUnit_ShouldReturnOk() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/business-units/1/reactivate")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void deleteBusinessUnit_ShouldReturnNoContent() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/v1/business-units/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void getBusinessUnitCount_ShouldReturnCount() throws Exception {
        // Given
        when(businessUnitService.count()).thenReturn(50L);

        // When/Then
        mockMvc.perform(get("/api/v1/business-units/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }
}
