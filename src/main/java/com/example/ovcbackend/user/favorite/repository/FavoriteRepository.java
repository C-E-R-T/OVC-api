package com.example.ovcbackend.user.favorite.repository;

import com.example.ovcbackend.user.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // certificate를 즉시 로딩해 N+1을 피하고, 최근 찜 순으로 내려줌
    @Query("select f from Favorite f join fetch f.certificate where f.user.id = :userId order by f.createdAt desc")
    List<Favorite> findAllByUserIdWithCertificate(@Param("userId") Long userId);

    // userId와 자격증Id 복합키가 존재 하냐 중복체크용
    boolean existsByUser_IdAndCertificate_Id(Long userId, Long certId);

    //userId와 자격증 Id 복합키 삭제
    long deleteByUser_IdAndCertificate_Id(Long userId, Long certId);
}
