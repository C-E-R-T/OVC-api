package com.example.ovcbackend.user.service;

import com.example.ovcbackend.user.dto.UserResponse;
import com.example.ovcbackend.user.entity.User;
import com.example.ovcbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserResponse getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        return UserResponse.from(user);
    }
}
