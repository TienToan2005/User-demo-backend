package com.example.user_demo.controller;

import com.example.user_demo.dto.request.UserRequest;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.enums.RoleUser;
import com.example.user_demo.enums.UserStatus;
import com.example.user_demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerSecuritySmokeTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;

    // ===== PUT /api/users/{id} - ADMIN only =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_admin_should200() throws Exception {
        UserRequest req = UserRequest.builder()
                .fullName("New Name")
                .email("tientoan@gmail.com")
                .build();

        UserResponse res = new UserResponse(
                5L, "tientoan@gmail.com", "New Name",
                UserStatus.ACTIVE, RoleUser.USER, null
        );
        when(userService.update(eq(5L), any(UserRequest.class))).thenReturn(res);

        mockMvc.perform(put("/api/users/{id}", 5L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(userService).update(eq(5L), any(UserRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void update_user_should403() throws Exception {
        UserRequest req = UserRequest.builder()
                .fullName("New Name")
                .email("tientoan@gmail.com")
                .build();

        mockMvc.perform(put("/api/users/{id}", 5L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // ===== DELETE /api/users/{id} - ADMIN only =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_admin_should204() throws Exception {
        doNothing().when(userService).delete(5L);

        mockMvc.perform(delete("/api/users/{id}", 5L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).delete(5L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_user_should403() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 5L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // ===== GET /api/users - ADMIN only =====

    @Test
    @WithMockUser(roles = "USER")
    void getAll_user_should403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }
}