package com.example.ovcbackend.user.service;

import com.example.ovcbackend.auth.repository.RefreshTokenRepository;
import com.example.ovcbackend.user.dto.UserResponse;
import com.example.ovcbackend.user.dto.UserUpdateRequest;
import com.example.ovcbackend.user.entity.User;
import com.example.ovcbackend.user.exception.UserConflictException;
import com.example.ovcbackend.user.exception.UserNotFoundException;
import com.example.ovcbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    @Override
    public UserResponse getMyInfo(String email) {
        log.info("[getMyInfo] 내 정보 조회 시도 -Email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                        log.error("[getMyInfo] 사용자 조회 실패 - 존재하지 않는 이메일: {}", email);
                        return new UserNotFoundException("해당 사용자를 찾을 수 없습니다.");
                });

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMyInfo(String email, UserUpdateRequest userUpdateRequest) {
        log.info("[updateMyInfo] 내 정보 수정 시작 - Email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->{
                    log.error("[updateMyInfo] 사용자 조회 실패 - Email: {}", email);
                    return new UserNotFoundException("사용자를 찾을 수 없습니다.");
                });

       if(userUpdateRequest.getNickname() != null && !user.getNickname().equals(userUpdateRequest.getNickname())){
           if(userRepository.existsByNickname(userUpdateRequest.getNickname())) {
               log.warn("[updateMyInfo] 닉네임 중복 발생 - Nickname: {}", userUpdateRequest.getNickname());
               throw new UserConflictException("이미 사용 중인 닉네임입니다.");
           }
       }

        user.update(userUpdateRequest.getNickname());
        log.info("[updateMyInfo] 내 정보 수정 완료 - Email: {}", email);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        log.info("[deleteUser] 회원 탈퇴 시도 - Email: {}", email);

        // 리프레시 토큰 삭제
        refreshTokenRepository.deleteByEmail(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[deleteUser] 탈퇴 실패: 존재하지 않는 사용자 - Email: {}", email);
                    return new UserNotFoundException("존재하지 않는 사용자입니다.");
                });

        userRepository.delete(user);
        log.info("[deleteUser] 회원 탈퇴 처리 완료 - Email: {}", email);
    }

}
