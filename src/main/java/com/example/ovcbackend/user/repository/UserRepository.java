package com.example.ovcbackend.user.repository;

import com.example.ovcbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이름이 존재하는가?
    boolean existsByEmail(String email);
}
