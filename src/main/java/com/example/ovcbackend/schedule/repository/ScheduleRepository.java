package com.example.ovcbackend.schedule.repository;

import com.example.ovcbackend.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 월별 일정 조회용
    @Query(
            "SELECT s FROM Schedule s "
                    + "JOIN FETCH s.certificate c "
                    + "WHERE s.examAt BETWEEN:start AND :end "
                    + "OR s.applyStartAt BETWEEN :start AND :end "
                    + "OR s.resultAt BETWEEN :start AND :end"
    )
    List<Schedule> findAllByMonth(@Param("start")LocalDateTime start, @Param("end") LocalDateTime end);

    // 찜 목록 카드 생성용으로 cert에 매핑되는 스케줄을 한 번에 조회
    // (이벤트 타입 분류/대표 일정 선택은 서비스 계층에서 수행)
    @Query(
            "SELECT s FROM Schedule s "
                    + "JOIN FETCH s.certificate c "
                    + "WHERE c.id IN :certIds "
                    + "ORDER BY c.id ASC, s.applyStartAt ASC"
    )
    List<Schedule> findSchedulesByCertIds(@Param("certIds") List<Long> certIds);

}
