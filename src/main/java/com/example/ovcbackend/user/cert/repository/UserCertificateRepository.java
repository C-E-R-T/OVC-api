package com.example.ovcbackend.user.cert.repository;

import com.example.ovcbackend.user.cert.entity.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCertificateRepository extends JpaRepository<UserCertificate, Long> {

    @Query("select uc from UserCertificate uc join fetch uc.certificate where uc.user.id = :userId order by uc.createdAt desc")
    List<UserCertificate> findAllByUserIdWithCertificate(@Param("userId") Long userId);

    boolean existsByUser_IdAndCertificate_Id(Long userId, Long certId);

    long deleteByUser_IdAndCertificate_Id(Long userId, Long certId);
}
