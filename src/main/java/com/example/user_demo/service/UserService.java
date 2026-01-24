package com.example.user_demo.service;

import com.example.user_demo.dto.request.UserRequest;
import com.example.user_demo.dto.response.PageResponse;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.entity.User;
import com.example.user_demo.enums.ErrorCode;
import com.example.user_demo.enums.UserStatus;
import com.example.user_demo.exception.AppException;
import com.example.user_demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    private UserResponse toUserResponse(User user){
        Long id = user.getId();
        String email = user.getEmail();
        String fullName = user.getFullName();
        UserStatus status = user.getStatus();
        return new UserResponse(id,email,fullName,status);
    }

    public UserResponse create(UserRequest request){
        String email = request.email().trim().toLowerCase();
        String fullname = request.fullName().trim();
        if (userRepository.existsByEmail(email)){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullname);
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }
    public UserResponse getById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(user.getStatus() == UserStatus.DELETED){
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return toUserResponse(user);

    }
    public PageResponse<UserResponse> getUsers(String keyword, UserStatus status, Pageable pageable) {
        if(status == UserStatus.DELETED){
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        Page<UserResponse> page = userRepository
                .search(keyword, status, pageable)
                .map(this::toUserResponse);

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public UserResponse update(Long id, UserRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        user.setFullName(request.fullName().trim());
        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }
    public void delete(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(user.getStatus() == UserStatus.DELETED){
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }
}
