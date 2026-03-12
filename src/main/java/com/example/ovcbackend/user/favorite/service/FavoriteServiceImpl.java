package com.example.ovcbackend.user.favorite.service;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.certificate.repository.CertificateRepository;
import com.example.ovcbackend.user.entity.User;
import com.example.ovcbackend.user.favorite.dto.FavoriteResponse;
import com.example.ovcbackend.user.favorite.entity.Favorite;
import com.example.ovcbackend.user.favorite.exception.FavoriteConflictException;
import com.example.ovcbackend.user.favorite.exception.FavoriteNotFoundException;
import com.example.ovcbackend.user.favorite.repository.FavoriteRepository;
import com.example.ovcbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;

    @Override
    public List<FavoriteResponse> getFavorites(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new FavoriteNotFoundException("존재하지 않는 사용자입니다."));

        return favoriteRepository.findAllByUserIdWithCertificate(userId).stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void addFavorite(Long userId, Long certId) {
        // FK 대상 존재 여부를 먼저 검증해서 명확한 비즈니스 에러를 반환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FavoriteNotFoundException("존재하지 않는 사용자입니다."));

        Certificate certificate = certificateRepository.findById(certId)
                .orElseThrow(() -> new FavoriteNotFoundException("존재하지 않는 자격증입니다."));

        if (favoriteRepository.existsByUser_IdAndCertificate_Id(userId, certId)) {
            throw new FavoriteConflictException("이미 찜한 자격증입니다.");
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .certificate(certificate)
                .build();

        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long certId) {
        // deleteBy... 반환값으로 실제 삭제 여부를 판별
        long deletedCount = favoriteRepository.deleteByUser_IdAndCertificate_Id(userId, certId);
        if (deletedCount == 0) {
            throw new FavoriteNotFoundException("찜한 자격증이 아닙니다.");
        }
    }
}
