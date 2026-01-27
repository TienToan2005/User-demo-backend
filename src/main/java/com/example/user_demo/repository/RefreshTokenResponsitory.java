package com.example.user_demo.repository;

import com.example.user_demo.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenResponsitory extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("""
        update RefreshToken rt
        set rt.revoked = true
        where rt.user.id = :userId and rt.revoked = false
    """)
    void revokeALlByUser(@Param("userId") Long userId);

}
