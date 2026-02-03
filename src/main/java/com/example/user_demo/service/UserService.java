package com.example.user_demo.service;

import com.example.user_demo.dto.request.UpdateUserRequest;
import com.example.user_demo.dto.request.UserRequest;
import com.example.user_demo.dto.response.PageResponse;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.entity.User;
import com.example.user_demo.enums.ErrorCode;
import com.example.user_demo.enums.UserStatus;
import com.example.user_demo.exception.AppException;
import com.example.user_demo.mapper.UserMapper;
import com.example.user_demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static com.example.user_demo.enums.UserStatus.ACTIVE;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse create(UserRequest request){
        if (userRepository.existsByEmail(request.email())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setFullName(request.fullName().trim());
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }
    public UserResponse getById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toResponse(user);

    }
    public PageResponse<UserResponse> getUsers(String keyword, UserStatus status, Pageable pageable) {
        Page<UserResponse> page = userRepository
                .search(keyword, status, pageable)
                .map(userMapper::toResponse);

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public UserResponse updateMe(String email, UpdateUserRequest request){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        userMapper.updateEntity(request, user);

        if (request.email() != null) {
            user.setEmail(request.email().trim().toLowerCase());
        }
        if (request.fullname() != null) {
            user.setFullName(request.fullname().trim());
        }

        User saved = userRepository.save(user);
        userRepository.save(user);
        return userMapper.toResponse(saved);
    }
    public UserResponse update(Long id, UserRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != ACTIVE) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        user.setEmail(request.email().trim().toLowerCase());
        user.setFullName(request.fullName().trim());
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }
    public void delete(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }
}
