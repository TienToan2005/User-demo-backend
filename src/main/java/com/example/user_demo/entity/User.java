package com.example.user_demo.entity;

import com.example.user_demo.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String email;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private UserStatus status;
}
