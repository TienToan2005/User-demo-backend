package com.example.user_demo.entity;

import com.example.user_demo.enums.RoleUser;
import com.example.user_demo.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User extends BaseEnity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String email;

    private String fullName;

    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @Enumerated(EnumType.STRING)
    private RoleUser role;
    @Column(name = "avatar_url", nullable = true)
    private String avatarUrl;
}
