package com.example.ovcbackend.user.favorite.service;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.certificate.repository.CertificateRepository;
import com.example.ovcbackend.schedule.entity.Schedule;
import com.example.ovcbackend.schedule.repository.ScheduleRepository;
import com.example.ovcbackend.user.entity.User;
import com.example.ovcbackend.user.favorite.dto.FavoriteResponse;
import com.example.ovcbackend.user.favorite.entity.Favorite;
import com.example.ovcbackend.user.favorite.exception.FavoriteConflictException;
import com.example.ovcbackend.user.favorite.exception.FavoriteNotFoundException;
import com.example.ovcbackend.user.favorite.repository.FavoriteRepository;
import com.example.ovcbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    public List<FavoriteResponse> getFavorites(Long userId) {
        log.info("[getFavorites] 관심 자격증 목록 조회 시작 - UserId: {}", userId );
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[getFavorite] 사용자 조회 실패 - 존재하지 않는 UserId: {}", userId);
                    return new FavoriteNotFoundException("존재하지 않는 사용자입니다.");
                });

        List<Favorite> favorites = favoriteRepository.findAllByUserIdWithCertificate(userId);
        if (favorites.isEmpty()) {
            log.info("[getFavorites] 관심 자격증이 비어 있음 - UserId: {}", userId);
            return List.of();
        }

        log.info("[getFavorites] 관심 자격증 {}건 조회됨 - UserId: {}", favorites.size(), userId);
        List<Long> certIds = favorites.stream()
                .map(favorite -> favorite.getCertificate().getId())
                .distinct()
                .toList();

        List<Schedule> schedules = scheduleRepository.findSchedulesByCertIds(certIds);
        // certId 기준으로 묶어 각 자격증의 대표 카드 이벤트를 고르기 쉽게 만든다.
        Map<Long, List<Schedule>> schedulesByCertId = schedules.stream()
                .collect(Collectors.groupingBy(schedule -> schedule.getCertificate().getId()));

        return favorites.stream()
                .map(favorite -> {
                    // 자격증별로 카드에 표시할 이벤트 1건(APPLY/EXAM/RESULT 중)을 선택
                    EventCandidate candidate = selectCardEvent(
                            schedulesByCertId.getOrDefault(favorite.getCertificate().getId(), List.of())
                    );
                    if (candidate == null) {
                        return FavoriteResponse.from(favorite, null, null, null);
                    }
                    return FavoriteResponse.from(
                            favorite,
                            candidate.type(),
                            candidate.startDate().toString(),
                            candidate.endDate().toString()
                    );
                })
                .toList();
    }

    private EventCandidate selectCardEvent(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return null;
        }

        LocalDate today = LocalDate.now();
        List<EventCandidate> active = new ArrayList<>();
        List<EventCandidate> upcoming = new ArrayList<>();
        List<EventCandidate> past = new ArrayList<>();

        for (Schedule schedule : schedules) {
            // 프론트 카드 타입에 맞춰 3종 이벤트 후보를 만든다.
            // exam get
            addCandidate(active, upcoming, past, today, "APPLY",
                    schedule.getApplyStartAt().toLocalDate(), schedule.getApplyEndAt().toLocalDate());
            addCandidate(active, upcoming, past, today, "EXAM",
                    schedule.getExamStartAt().toLocalDate(), schedule.getExamEndAt().toLocalDate());
            addCandidate(active, upcoming, past, today, "RESULT",
                    schedule.getResultAt().toLocalDate(), schedule.getResultAt().toLocalDate());
        }

        // 카드 노출 우선순위: 진행중 > 예정 > 지난 일정
        if (!active.isEmpty()) {
            return active.stream()
                    .sorted((a, b) -> a.endDate().compareTo(b.endDate()))
                    .findFirst()
                    .orElse(null);
        }

        if (!upcoming.isEmpty()) {
            return upcoming.stream()
                    .sorted((a, b) -> a.startDate().compareTo(b.startDate()))
                    .findFirst()
                    .orElse(null);
        }

        return past.stream()
                .sorted((a, b) -> b.endDate().compareTo(a.endDate()))
                .findFirst()
                .orElse(null);
    }

    private void addCandidate(List<EventCandidate> active, List<EventCandidate> upcoming,
                              List<EventCandidate> past, LocalDate today,
                              String type, LocalDate start, LocalDate end) {
        EventCandidate candidate = new EventCandidate(type, start, end);

        // 오늘이 구간 내면 진행중, 시작 전이면 예정, 그 외는 지난 일정으로 분류
        if (!today.isBefore(start) && !today.isAfter(end)) {
            active.add(candidate);
            return;
        }

        if (today.isBefore(start)) {
            upcoming.add(candidate);
            return;
        }

        past.add(candidate);
    }

    private record EventCandidate(String type, LocalDate startDate, LocalDate endDate) {}

    @Override
    @Transactional
    public void addFavorite(Long userId, Long certId) {
        log.info("[addFavorite] 관심 자격증 추가 시도 - UserId: {}, CertId: {}", userId, certId);
        // FK 대상 존재 여부를 먼저 검증해서 명확한 비즈니스 에러를 반환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[addFavorite] 추가 실패: 존재하지 않는 사용자입니다. - UserId: {}", userId);
                    return new FavoriteNotFoundException("존재하지 않는 사용자입니다.");
                });

        Certificate certificate = certificateRepository.findById(certId)
                .orElseThrow(() -> {
                    log.error("[addFavorite] 추가 실패: 존재 하지 않는 자격증 - CertId: {}", certId);
                    return  new FavoriteNotFoundException("존재하지 않는 자격증입니다.");
                });

        if (favoriteRepository.existsByUser_IdAndCertificate_Id(userId, certId)) {
            log.warn("[addFavorite] 추가 실패: 이미 찜한 자격증 - UserId, certId: {}", certId);
            throw new FavoriteConflictException("이미 찜한 자격증입니다.");
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .certificate(certificate)
                .build();

        favoriteRepository.save(favorite);
        log.info("[addFavorite] 관심 자격증 추가 완료 - FavoriteId: {}", favorite.getId());
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long certId) {
        log.info("[removeFavorite] 관심 자격증 삭제 시도 - UserId: {}, CertId: {}", userId, certId);
        // deleteBy... 반환값으로 실제 삭제 여부를 판별
        long deletedCount = favoriteRepository.deleteByUser_IdAndCertificate_Id(userId, certId);
        if (deletedCount == 0) {
            log.warn("[removeFavorite] 삭제 실패: 찜한 내역 없음 - UserId: {}, CertId: {}", userId, certId);
            throw new FavoriteNotFoundException("찜한 자격증이 아닙니다.");
        }
        log.info("[removeFavorite] 관심 자격증 삭제 완료 - UserId: {}, CertId: {}", userId, certId);
    }
}
