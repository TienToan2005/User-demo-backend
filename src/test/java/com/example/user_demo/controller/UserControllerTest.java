package com.example.user_demo.controller;

import com.example.user_demo.dto.request.UserRequest;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.enums.RoleUser;
import com.example.user_demo.enums.UserStatus;
import com.example.user_demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = {
                // ✅ disable ALL security
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,

                // ✅ disable ALL datasource/jpa (avoid jpaAuditingHandler)
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerWebMvcTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedEmail(String email) {
        var auth = new UsernamePasswordAuthenticationToken(email, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getMe_success() throws Exception {
        setAuthenticatedEmail("tientoan@gmail.com");

        UserResponse res = new UserResponse(
                5L, "tientoan@gmail.com", "Hoang Tien Toan",
                UserStatus.ACTIVE, RoleUser.USER, "avatar.png"
        );
        when(userService.getByEmail("tientoan@gmail.com")).thenReturn(res);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.result.id").value(5))
                .andExpect(jsonPath("$.result.email").value("tientoan@gmail.com"));

        verify(userService).getByEmail("tientoan@gmail.com");
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getById_success() throws Exception {
        UserResponse res = new UserResponse(
                5L, "tientoan@gmail.com", "Hoang Tien Toan",
                UserStatus.ACTIVE, RoleUser.USER, null
        );
        when(userService.getById(5L)).thenReturn(res);

        mockMvc.perform(get("/api/users/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.result.id").value(5));

        verify(userService).getById(5L);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void update_success() throws Exception {
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.result.fullName").value("New Name"));

        verify(userService).update(eq(5L), any(UserRequest.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void delete_success() throws Exception {
        doNothing().when(userService).delete(5L);

        mockMvc.perform(delete("/api/users/{id}", 5L))
                .andExpect(status().isNoContent());

        verify(userService).delete(5L);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void uploadAvatar_success() throws Exception {
        setAuthenticatedEmail("tientoan@gmail.com");

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "fake".getBytes()
        );

        UserResponse res = new UserResponse(
                5L, "tientoan@gmail.com", "Hoang Tien Toan",
                UserStatus.ACTIVE, RoleUser.USER, "new.png"
        );

        when(userService.updateMyAvatar(eq("tientoan@gmail.com"), any())).thenReturn(res);

        mockMvc.perform(multipart("/api/users/me/avatar")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.result.avatarUrl").value("new.png"));

        verify(userService).updateMyAvatar(eq("tientoan@gmail.com"), any());
        verifyNoMoreInteractions(userService);
    }
}