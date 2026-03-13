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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyCertServiceImpl implements MyCertService {

    private final UserCertificateRepository userCertificateRepository;
    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;

    @Override
    public List<MyCertResponse> getMyCerts(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new MyCertNotFoundException("존재하지 않는 사용자입니다."));

        return userCertificateRepository.findAllByUserIdWithCertificate(userId).stream()
                .map(MyCertResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void addMyCert(Long userId, Long certId, MyCertRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MyCertNotFoundException("존재하지 않는 사용자입니다."));

        Certificate certificate = certificateRepository.findById(certId)
                .orElseThrow(() -> new MyCertNotFoundException("존재하지 않는 자격증입니다."));

        if (userCertificateRepository.existsByUser_IdAndCertificate_Id(userId, certId)) {
            throw new MyCertConflictException("이미 등록한 자격증입니다.");
        }

        UserCertificate userCertificate = UserCertificate.builder()
                .user(user)
                .certificate(certificate)
                .certNumber(request.getCertNumber())
                .passedAt(request.getPassedAt())
                .expiredAt(request.getExpiredAt())
                .build();

        userCertificateRepository.save(userCertificate);
    }

    @Override
    @Transactional
    public void removeMyCert(Long userId, Long certId) {
        long deleted = userCertificateRepository.deleteByUser_IdAndCertificate_Id(userId, certId);
        if (deleted == 0) {
            throw new MyCertNotFoundException("등록된 자격증이 아닙니다.");
        }
    }
}
