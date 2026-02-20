package com.example.user_demo.service;

import com.example.user_demo.dto.request.RegisterRequest;
import com.example.user_demo.dto.request.UserRequest;
import com.example.user_demo.dto.response.PageResponse;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.entity.User;
import com.example.user_demo.enums.ErrorCode;
import com.example.user_demo.enums.RoleUser;
import com.example.user_demo.enums.UserStatus;
import com.example.user_demo.exception.AppException;
import com.example.user_demo.mapper.UserMapper;
import com.example.user_demo.repository.UserRepository;
import com.example.user_demo.service.storage.FileStorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private FileStorageService fileStorageService;
    private User user;
    private UserRequest userRequest;
    private UserResponse userResponse;
    private RegisterRequest registerRequest;
    @BeforeEach
    void initData(){
        user = User.builder()
                .id(5L)
                .email("tientoan@gmail.com")
                .status(UserStatus.ACTIVE)
                .fullName("Hoang Tien Toan")
                .role(RoleUser.USER)
                .password("hashed-pass")
                .build();
        userRequest = UserRequest.builder()
                .fullName("Hoang Tien Toan")
                .email("tientoan@gmail.com")
                .build();
        userResponse = UserResponse.builder()
                .id(5L)
                .email("tientoan@gmail.com")
                .status(UserStatus.ACTIVE)
                .fullName("Hoang Tien Toan")
                .roleUser(RoleUser.USER)
                .build();
        registerRequest = RegisterRequest.builder()
                .email("httoan@gmail.com")
                .fullName("Hoang Tien Toan")
                .password("123456")
                .build();
    }
    @Test
    void create_success(){
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(user);

        User user = userService.createUser(registerRequest.email(),registerRequest.fullName(),registerRequest.password());

        assertNotNull(user);
        assertEquals(5L,user.getId());
        assertEquals("tientoan@gmail.com", user.getEmail());
        assertEquals("Hoang Tien Toan", user.getFullName());
        assertEquals(RoleUser.USER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository).existsByEmail("httoan@gmail.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("123456");
        verifyNoMoreInteractions(userRepository,passwordEncoder);
    }
    @Test
    void create_fail(){
        //GIVE
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        //WHEN
        var exception = assertThrows(AppException.class,
                () -> userService.createUser(registerRequest.email(),registerRequest.fullName(),registerRequest.password()));

        //THEN
        Assertions.assertEquals(ErrorCode.EMAIL_EXISTED, exception.getErrorCode());
    }
    @Test
    void getById_success(){
        //GIVEN
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        //WHEN
        UserResponse userResponse = userService.getById(5L);
        //THEN
        assertNotNull(userResponse);
        assertEquals(5L, userResponse.id());
        assertEquals("tientoan@gmail.com", userResponse.email());
        assertEquals("Hoang Tien Toan", userResponse.fullName());
        assertEquals(RoleUser.USER, userResponse.roleUser());
        assertEquals(UserStatus.ACTIVE, userResponse.status());
    }
    @Test
    void getById_fail_userNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class, () -> userService.getById(user.getId()));

        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    void getById_fail_userNotStatus() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        user.setStatus(UserStatus.DELETED);
        var exception = assertThrows(AppException.class, () -> userService.getById(user.getId()));

        Assertions.assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }
    @Test
    void getByEmail_success(){
        //GIVEN
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));
        //WHEN
        UserResponse userResponse = userService.getByEmail(user.getEmail());
        //THEN
        assertNotNull(userResponse);
        assertEquals(5L, userResponse.id());
        assertEquals("tientoan@gmail.com", userResponse.email());
        assertEquals("Hoang Tien Toan", userResponse.fullName());
        assertEquals(RoleUser.USER, userResponse.roleUser());
        assertEquals(UserStatus.ACTIVE, userResponse.status());
    }
    @Test
    void getByEmail_fail_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class, () -> userService.getByEmail(user.getEmail()));

        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    void getByEmail_fail_userNotStatus() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        user.setStatus(UserStatus.DELETED);
        var exception = assertThrows(AppException.class, () -> userService.getByEmail(user.getEmail()));

        Assertions.assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }
    @Test
    void getUsers_success() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 5);
        String keyword = "toan";
        UserStatus status = UserStatus.ACTIVE;

        User u1 = User.builder()
                .id(1L).email("a@gmail.com").fullName("A")
                .status(UserStatus.ACTIVE).role(RoleUser.USER)
                .avatarUrl(null)
                .build();

        User u2 = User.builder()
                .id(2L).email("b@gmail.com").fullName("B")
                .status(UserStatus.ACTIVE).role(RoleUser.ADMIN)
                .avatarUrl("x.png")
                .build();

        Page<User> page = new PageImpl<>(List.of(u1, u2), pageable, 2);

        when(userRepository.search(keyword, status, pageable)).thenReturn(page);

        // WHEN
        PageResponse<UserResponse> res = userService.getUsers(keyword, status, pageable);

        // THEN
        assertNotNull(res);
        assertEquals(2, res.items().size());
        assertEquals(0, res.page());
        assertEquals(5, res.size());
        assertEquals(2, res.totalItems());
        assertEquals(1, res.totalPages());

        // check mapping item 1
        UserResponse r1 = res.items().getFirst();
        assertEquals(1L, r1.id());
        assertEquals("a@gmail.com", r1.email());
        assertEquals("A", r1.fullName());
        assertEquals(UserStatus.ACTIVE, r1.status());
        assertEquals(RoleUser.USER, r1.roleUser());

        verify(userRepository).search(keyword, status, pageable);
    }
    @Test
    void getUsers_fail_statusDeleted() {
        Pageable pageable = PageRequest.of(0, 5);

        AppException ex = assertThrows(
                AppException.class,
                () -> userService.getUsers(null, UserStatus.DELETED, pageable)
        );

        assertEquals(ErrorCode.BAD_REQUEST, ex.getErrorCode());
        verify(userRepository, never()).search(any(), any(), any());
    }
    @Test
    void update_success(){
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse userResponse = userService.update(user.getId(),userRequest);

        assertEquals("tientoan@gmail.com", userResponse.email());
        assertEquals("Hoang Tien Toan", userResponse.fullName());
        verify(userRepository).save(any());
    }
    @Test
    void update_fail_userNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userService.update(5L, userRequest));

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        verify(userRepository).findById(5L);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }
    @Test
    void delete_success() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.delete(5L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).findById(5L);
        verify(userRepository).save(captor.capture());
        assertEquals(UserStatus.DELETED, captor.getValue().getStatus());

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_fail_userNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userService.delete(5L));

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        verify(userRepository).findById(5L);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_fail_statusNotActive() {
        user.setStatus(UserStatus.DELETED);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        AppException ex = assertThrows(AppException.class, () -> userService.delete(5L));

        assertEquals(ErrorCode.BAD_REQUEST, ex.getErrorCode());
        verify(userRepository).findById(5L);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

}

