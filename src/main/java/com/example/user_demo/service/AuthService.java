package com.example.user_demo.service;

import com.example.user_demo.config.JwtService;
import com.example.user_demo.dto.request.LoginRequest;
import com.example.user_demo.dto.request.RegisterRequest;
import com.example.user_demo.dto.request.TokenRequest;
import com.example.user_demo.dto.response.ApiResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenResponsitory refreshTokenResponsitory;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;
    public AuthService(UserRepository userRepository, RefreshTokenResponsitory refreshTokenResponsitory, PasswordEncoder passwordEncoder, JwtService jwtService, UserService userService) {
        this.userRepository = userRepository;
        this.refreshTokenResponsitory = refreshTokenResponsitory;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public TokenResponse login(LoginRequest request){
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if(user.getStatus() != UserStatus.ACTIVE){
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        boolean matched = passwordEncoder.matches(request.password(), user.getPassword());
        if(!matched){
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        String token = jwtService.generateToken(user);

        refreshTokenResponsitory.revokeALlByUser(user.getId());

        String refreshtoken = UUID.randomUUID().toString();
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(refreshtoken);
        rt.setCreatedAt(Instant.now());
        rt.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        rt.setRevoked(false);
        refreshTokenResponsitory.save(rt);
        return new TokenResponse(token,refreshtoken,jwtService.getExpirationSeconds());
    }
    public UserResponse register(RegisterRequest request) {

        User user = userService.createUser(
                request.email(),
                request.fullName(),
                request.password()
        );

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                user.getRole(),
                user.getAvatarUrl()
        );
    }
    public TokenResponse refresh(TokenRequest request){
        RefreshToken rt = refreshTokenResponsitory.findByToken(request.refreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if(rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())){
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        User user = rt.getUser();
        // revoke token cu
        rt.setRevoked(true);
        refreshTokenResponsitory.save(rt);

        //tao token moi
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = UUID.randomUUID().toString();

        RefreshToken newRT = new RefreshToken();
        newRT.setUser(user);
        newRT.setToken(newRefreshToken);
        newRT.setCreatedAt(Instant.now());
        newRT.setExpiresAt(Instant.now().plus(30,ChronoUnit.DAYS));
        newRT.setRevoked(false);

        refreshTokenResponsitory.save(newRT);

        return new TokenResponse(newAccessToken,newRefreshToken, jwtService.getExpirationSeconds());
    }
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        refreshTokenResponsitory.revokeALlByUser(user.getId());
    }

}
