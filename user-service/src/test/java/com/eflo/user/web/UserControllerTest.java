package com.eflo.user.web;

import com.eflo.user.domain.dto.CreateUserRequest;
import com.eflo.user.domain.dto.UpdateUserRequest;
import com.eflo.user.domain.dto.UserDTO;
import com.eflo.user.domain.entity.User;
import com.eflo.user.mapper.UserMapper;
import com.eflo.user.service.UserService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtAuthenticationHelper jwtHelper;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setKeycloakId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmployeeNumber("EMP001");
        testUser.setActive(true);

        testUserDTO = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .employeeNumber("EMP001")
                .isActive(true)
                .build();

        when(jwtHelper.getCurrentUsername()).thenReturn("admin");
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void createUser_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("new@example.com");
        request.setFirstName("New");
        request.setLastName("User");
        request.setTemporaryPassword("TempPass123!");

        when(userMapper.toEntity(any(CreateUserRequest.class))).thenReturn(testUser);
        when(userService.createUser(any(User.class), anyString(), anyString())).thenReturn(testUser);
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"));
    }

    @Test
    @WithMockUser
    void getUserById_ShouldReturnUser_WhenExists() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

        // When/Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void getUserById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getUserByEmail_ShouldReturnUser_WhenExists() throws Exception {
        // Given
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

        // When/Then
        mockMvc.perform(get("/api/v1/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void getUserByEmployeeNumber_ShouldReturnUser_WhenExists() throws Exception {
        // Given
        when(userService.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

        // When/Then
        mockMvc.perform(get("/api/v1/users/employee/EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeNumber").value("EMP001"));
    }

    @Test
    @WithMockUser
    void getAllUsers_ShouldReturnPagedResponse() throws Exception {
        // Given
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), PageRequest.of(0, 10), 1);
        when(userService.findAll(any(PageRequest.class))).thenReturn(userPage);
        when(userMapper.toDTOList(anyList())).thenReturn(Arrays.asList(testUserDTO));

        // When/Then
        mockMvc.perform(get("/api/v1/users?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser
    void getActiveUsers_ShouldReturnActiveUsers() throws Exception {
        // Given
        List<User> activeUsers = Arrays.asList(testUser);
        when(userService.findAllActive()).thenReturn(activeUsers);
        when(userMapper.toDTOList(anyList())).thenReturn(Arrays.asList(testUserDTO));

        // When/Then
        mockMvc.perform(get("/api/v1/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void searchUsers_ShouldReturnMatchingUsers() throws Exception {
        // Given
        List<User> searchResults = Arrays.asList(testUser);
        when(userService.searchUsers("test")).thenReturn(searchResults);
        when(userMapper.toDTOList(anyList())).thenReturn(Arrays.asList(testUserDTO));

        // When/Then
        mockMvc.perform(get("/api/v1/users/search?q=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Test"));
    }

    @Test
    @WithMockUser
    void getUsersByDepartment_ShouldReturnDepartmentUsers() throws Exception {
        // Given
        List<User> deptUsers = Arrays.asList(testUser);
        when(userService.findByDepartment("IT")).thenReturn(deptUsers);
        when(userMapper.toDTOList(anyList())).thenReturn(Arrays.asList(testUserDTO));

        // When/Then
        mockMvc.perform(get("/api/v1/users/department/IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void updateUser_ShouldReturnUpdated_WhenValidRequest() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");

        when(userMapper.toEntity(any(CreateUserRequest.class))).thenReturn(testUser);
        when(userService.updateUser(anyLong(), any(User.class), anyString())).thenReturn(testUser);
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

        // When/Then
        mockMvc.perform(put("/api/v1/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void deactivateUser_ShouldReturnOk() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/users/1/deactivate")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void reactivateUser_ShouldReturnOk() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/users/1/reactivate")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void deleteUser_ShouldReturnNoContent() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/v1/users/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void resetPassword_ShouldReturnOk() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/users/1/reset-password")
                .with(csrf())
                .param("newPassword", "NewPass123!")
                .param("temporary", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void recordLogin_ShouldReturnOk() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/users/1/login")
                .with(csrf())
                .param("ipAddress", "192.168.1.1")
                .param("userAgent", "Mozilla/5.0"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void getUserCount_ShouldReturnCount() throws Exception {
        // Given
        when(userService.count()).thenReturn(100L);

        // When/Then
        mockMvc.perform(get("/api/v1/users/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void getActiveUserCount_ShouldReturnCount() throws Exception {
        // Given
        when(userService.countActive()).thenReturn(85L);

        // When/Then
        mockMvc.perform(get("/api/v1/users/count/active"))
                .andExpect(status().isOk())
                .andExpect(content().string("85"));
    }
}
