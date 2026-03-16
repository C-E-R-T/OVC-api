package com.example.ovcbackend.user.cert.service;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.certificate.repository.CertificateRepository;
import com.example.ovcbackend.user.cert.dto.MyCertRequest;
import com.example.ovcbackend.user.cert.dto.MyCertResponse;
import com.example.ovcbackend.user.cert.entity.UserCertificate;
import com.example.ovcbackend.user.cert.exception.MyCertConflictException;
import com.example.ovcbackend.user.cert.exception.MyCertNotFoundException;
import com.example.ovcbackend.user.cert.repository.UserCertificateRepository;
import com.example.ovcbackend.user.entity.User;
import com.example.ovcbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyCertServiceImpl implements MyCertService {

    private final UserCertificateRepository userCertificateRepository;
    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;

    @Override
    public List<MyCertResponse> getMyCerts(Long userId) {
        log.info("[getMyCerts] 내 자격증 목록 조회 시작 - UserId: {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[getMyCerts] 사용자 조회 실패 - 존재하지 않는 UserId: {}", userId);
                    return new MyCertNotFoundException("존재하지 않는 사용자입니다.");
                });

        List<UserCertificate> certs = userCertificateRepository.findAllByUserIdWithCertificate(userId);
        log.info("[getMyCerts] 내 자격증 {}건 조회됨 - userId: {}", certs.size(), userId);
        return certs.stream()
                .map(MyCertResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void addMyCert(Long userId, Long certId, MyCertRequest request) {
        log.info("[addMyCert] 내 자격증 등록 시도 - userId: {}, certId: {}", userId, certId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[addMyCert] 등록 실패: 존재하지 않는 사요자 - userId: {}", userId);
                    return new MyCertNotFoundException("존재하지 않는 사용자입니다.");
                });

        Certificate certificate = certificateRepository.findById(certId)
                .orElseThrow(() -> {
                    log.error("[addMyCert] 등록 실패: 존재하지 않는 자격증 - CertId: {}", certId);
                    return new MyCertNotFoundException("존재하지 않는 자격증입니다.");
                });

        if (userCertificateRepository.existsByUser_IdAndCertificate_Id(userId, certId)) {
            log.warn("[addMyCert] 등록 실패: 이미 등록 자격증 - UserId: {}, CertId: {}", userId, certId);
            throw new MyCertConflictException("이미 등록한 자격증입니다.");
        }

        log.info("[addMyCert] 자격증 상세 정보 확인 - CertNumber: {}, PassedAt: {}",
                request.getCertNumber(), request.getPassedAt());
        UserCertificate userCertificate = UserCertificate.builder()
                .user(user)
                .certificate(certificate)
                .certNumber(request.getCertNumber())
                .passedAt(request.getPassedAt())
                .expiredAt(request.getExpiredAt())
                .build();

        userCertificateRepository.save(userCertificate);
        log.info("[addMyCert 내 자격증 등록 완료 - UserCertificateId: {}", userCertificate.getId());
    }

    @Override
    @Transactional
    public void removeMyCert(Long userId, Long certId) {
        log.info("[removeMyCert] 내 자격증 삭제 시도 - UserId: {}, CertId: {}", userId, certId );
        long deleted = userCertificateRepository.deleteByUser_IdAndCertificate_Id(userId, certId);
        if (deleted == 0) {
            log.warn("[removeMyCert] 삭제 실패: 등록된 내역 없음 - UserId:{}, CertId: {}", userId, certId);
            throw new MyCertNotFoundException("등록된 자격증이 아닙니다.");
        }
        log.info("[removeMyCert] 내 자격증 삭제 완료 - UserId: {}, CertId: {}", userId, certId);
    }
}
