package com.example.user_demo.repository;

import com.example.user_demo.entity.User;
import com.example.user_demo.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
           select u from User u
           where(:keyword is null 
                   or lower(u.email) like lower(concat('%',:keyword,'%'))
                   or lower(u.fullName) like lower(concat('%',:keyword,'%')))
           and (:status is null or u.status = :status)       
    """)
    Page<User> search(
            @Param("keyword") String keyword,
            @Param("status") UserStatus status,
            Pageable pageable
    );
    boolean existsByEmail(String email);
}
