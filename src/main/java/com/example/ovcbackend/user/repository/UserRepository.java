package com.example.ovcbackend.user.repository;

import com.example.ovcbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이름이 존재하는가?
    boolean existsByEmail(String email);
    // 이메일로 유저 반환
    // optional을 사용하면 유저가 없으면 null이 아니라 empty를 반환해서 에러처리가 편해짐
    Optional<User> findByEmail(String email);
}
