package com.example.user_demo.service;

import com.example.user_demo.dto.request.UserRequest;
import com.example.user_demo.dto.response.PageResponse;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.entity.User;
import com.example.user_demo.enums.ErrorCode;
import com.example.user_demo.enums.RoleUser;
import com.example.user_demo.enums.UserStatus;
import com.example.user_demo.exception.AppException;
import com.example.user_demo.repository.UserRepository;
import com.example.user_demo.service.storage.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }
    private UserResponse toUserResponse(User user){
        Long id = user.getId();
        String email = user.getEmail();
        String fullName = user.getFullName();
        UserStatus status = user.getStatus();
        RoleUser role = user.getRole();
        String avatarUrl = user.getAvatarUrl();
        return new UserResponse(id,email,fullName,status,role,avatarUrl);
    }

    public UserResponse getById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(user.getStatus() != UserStatus.ACTIVE){
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        return toUserResponse(user);

    }
    public UserResponse getByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(user.getStatus() != UserStatus.ACTIVE){
            throw new AppException(ErrorCode.FORBIDDEN);
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
    public UserResponse updateMyAvatar(String email, MultipartFile multipartFile){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        fileStorageService.deleteAvatar(user.getAvatarUrl());
        String filename = fileStorageService.saveAvatar(user.getId(),multipartFile);
        user.setAvatarUrl(filename);
        User save = userRepository.save(user);
        return toUserResponse(save);
    }

}
