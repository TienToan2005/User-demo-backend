package com.example.user_demo.service;

import com.example.user_demo.config.JwtService;
import com.example.user_demo.dto.request.LoginRequest;
import com.example.user_demo.dto.request.RegisterRequest;
import com.example.user_demo.dto.request.TokenRequest;
import com.example.user_demo.dto.response.TokenResponse;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.entity.RefreshToken;
import com.example.user_demo.entity.User;
import com.example.user_demo.enums.ErrorCode;
import com.example.user_demo.enums.RoleUser;
import com.example.user_demo.enums.UserStatus;
import com.example.user_demo.exception.AppException;
import com.example.user_demo.repository.RefreshTokenResponsitory;
import com.example.user_demo.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks private AuthService authService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenResponsitory refreshTokenResponsitory;
    @Mock private UserService userService;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private User user;
    private RefreshToken refreshToken;
    private TokenRequest tokenRequest;

    @BeforeEach
    void initData() {
        registerRequest = RegisterRequest.builder()
                .email("httoan@gmail.com")
                .fullName("Hoang Tien Toan")
                .password("123456")
                .build();
        loginRequest = LoginRequest.builder()
                .email("httoan@gmail.com")
                .password("123456")
                .build();
        user = User.builder()
                .id(5L)
                .email("tientoan@gmail.com")
                .status(UserStatus.ACTIVE)
                .fullName("Hoang Tien Toan")
                .roleUser(RoleUser.USER)
                .password("hashed-pass")
                .build();
        UserResponse userResponse = UserResponse.builder()
                .id(5L)
                .email("tientoan@gmail.com")
                .status(UserStatus.ACTIVE)
                .fullName("Hoang Tien Toan")
                .roleUser(RoleUser.USER)
                .build();
        refreshToken = RefreshToken.builder()
                .id(1L)
                .token("a283cc27-0220-43e4-95f6-f353eef81382")
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .revoked(false)
                .user(user)
                .build();
        tokenRequest = new TokenRequest(refreshToken.getToken());
    }

    @Test
    void register_success() {
        // GIVEN
        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenReturn(user);

        // WHEN
        UserResponse res = authService.register(registerRequest);

        // THEN (assert mapping)
        assertNotNull(res);
        assertEquals(5L, res.id());
        assertEquals("tientoan@gmail.com", res.email());
        assertEquals("Hoang Tien Toan", res.fullName());
        assertEquals(RoleUser.USER, res.roleUser());
        assertEquals(UserStatus.ACTIVE, res.status());

        // verify gọi đúng
        verify(userService).createUser("httoan@gmail.com", "Hoang Tien Toan", "123456");

        // và đảm bảo authService không đụng repo/encoder
        verifyNoInteractions(userRepository, passwordEncoder);
    }
    @Test
    void login_success(){
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hashed-pass")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(refreshTokenResponsitory.save(any())).thenReturn(new RefreshToken());
        doNothing().when(refreshTokenResponsitory).revokeALlByUser(any());
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        TokenResponse tokenResponse = authService.login(loginRequest);

        assertNotNull(tokenResponse);
        assertEquals("access-token",tokenResponse.accessToken());
        assertNotNull(tokenResponse.refreshToken());
        assertTrue(tokenResponse.expiresIn() > 0);
        verify(userRepository).findByEmail("httoan@gmail.com");
        verify(passwordEncoder).matches("123456", user.getPassword());
        verify(jwtService).generateToken(user);
        verify(refreshTokenResponsitory).revokeALlByUser(user.getId());

    }
    @Test
    void login_fail_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class, () -> authService.login(loginRequest));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenResponsitory, never()).save(any());
        Assertions.assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
    }
    @Test
    void login_fail_userNotStatus() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        user.setStatus(UserStatus.DELETED);

        var exception = assertThrows(AppException.class, () -> authService.login(loginRequest));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenResponsitory, never()).save(any());
        Assertions.assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }
    @Test
    void login_fail_wrongpassword() {
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), nullable(String.class))).thenReturn(false);

        var exception =  assertThrows(AppException.class, () -> authService.login(loginRequest));

        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenResponsitory, never()).save(any());
        Assertions.assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
    }
    @Test
    void refresh_success() {
        // GIVEN
        when(refreshTokenResponsitory.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);
        when(refreshTokenResponsitory.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TokenResponse response = authService.refresh(tokenRequest);

        // THEN
        verify(refreshTokenResponsitory).findByToken(tokenRequest.refreshToken());
        verify(jwtService).generateToken(user);
        assertEquals("access-token", response.accessToken());
        assertNotNull(response.refreshToken());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenResponsitory, times(2)).save(captor.capture());
        List<RefreshToken> saved = captor.getAllValues();
        RefreshToken oldToken = saved.get(0);
        RefreshToken newToken = saved.get(1);

        assertTrue(oldToken.isRevoked());
        assertFalse(newToken.isRevoked());
        assertNotEquals(oldToken.getToken(), newToken.getToken());
    }
    @Test
    void refresh_fail_tokenNotFound(){
        when(refreshTokenResponsitory.findByToken(anyString())).thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class, () -> authService.refresh(tokenRequest));

        Assertions.assertEquals(ErrorCode.INVALID_CREDENTIALS,exception.getErrorCode());
        verify(refreshTokenResponsitory,never()).save(any());
    }
    @Test
    void  refresh_fail_revoked(){
        when(refreshTokenResponsitory.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        refreshToken.setRevoked(true);

        var exception = assertThrows(AppException.class, () -> authService.refresh(tokenRequest));

        Assertions.assertEquals(ErrorCode.INVALID_CREDENTIALS,exception.getErrorCode());
        verify(refreshTokenResponsitory,never()).save(any());
    }
    @Test
    void refresh_fail_expired(){
        when(refreshTokenResponsitory.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        refreshToken.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));

        var exception = assertThrows(AppException.class, () -> authService.refresh(tokenRequest));

        Assertions.assertEquals(ErrorCode.INVALID_CREDENTIALS,exception.getErrorCode());
        verify(refreshTokenResponsitory,never()).save(any());

    }
}
