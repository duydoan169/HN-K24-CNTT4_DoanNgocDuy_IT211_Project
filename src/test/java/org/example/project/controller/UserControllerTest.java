package org.example.project.controller;

import org.example.project.controller.UserController;
import org.example.project.exception.UserNotFoundException;
import org.example.project.model.dto.request.RegisterRequest;
import org.example.project.model.dto.request.UserUpdateRequest;
import org.example.project.model.dto.response.UserResponse;
import org.example.project.model.enums.Role;
import org.example.project.repository.TokenBlacklistRepository;
import org.example.project.security.JwtAuthFilter;
import org.example.project.security.JwtUtil;
import org.example.project.security.UserDetailsServiceImpl;
import org.example.project.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse mockResponse() {
        return UserResponse.builder()
                .id(1L)
                .username("patient1")
                .role(Role.PATIENT)
                .isActive(true)
                .build();
    }

    @Test
    void getAllUsers_ReturnsOk() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(mockResponse()));
        when(userService.getAllUsers(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUserById_ReturnsOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(mockResponse());

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("patient1"));
    }

    @Test
    void getUserById_NotFound_Returns404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/v1/admin/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ReturnsOk() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa người dùng thành công"));
    }

    @Test
    void updateUser_ReturnsOk() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updatedName");
        request.setRole(Role.DOCTOR);
        request.setIsActive(true);

        UserResponse updated = mockResponse();
        updated.setUsername("updatedName");

        when(userService.updateUser(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("updatedName"));
    }

    @Test
    void searchUsers_ReturnsOk() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(mockResponse()));
        when(userService.searchUsers("patient", 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users/search")
                        .param("username", "patient")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createUser_ReturnsCreated() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newdoctor");
        request.setPassword("123456");
        request.setRole(Role.DOCTOR);

        when(userService.register(any())).thenReturn(mockResponse());

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateUser_InvalidBody_Returns400() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();

        mockMvc.perform(put("/api/v1/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsersByRole_ReturnsOk() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(mockResponse()));
        when(userService.getUsersByRole(Role.PATIENT, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users/role")
                        .param("role", "PATIENT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateUser_NotFound_Returns404() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updatedName");
        request.setRole(Role.DOCTOR);
        request.setIsActive(true);

        when(userService.updateUser(eq(99L), any())).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(put("/api/v1/admin/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
