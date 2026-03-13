package com.example.ovcbackend.schedule.repository;

import com.example.ovcbackend.schedule.entity.Schedule;
import com.example.ovcbackend.schedule.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 월별 일정 조회용
    @Query(
            "SELECT s FROM Schedule s "
                    + "JOIN FETCH s.certificate c "
                    + "WHERE s.examStartAt BETWEEN :start AND :end "
                    + "OR s.examEndAt BETWEEN :start AND :end "
                    + "OR s.applyStartAt BETWEEN :start AND :end "
                    + "OR s.resultAt BETWEEN :start AND :end"
    )
    List<Schedule> findAllByMonth(@Param("start")LocalDateTime start, @Param("end") LocalDateTime end);

    Optional<Schedule> findByCertificateIdAndExamTypeAndExamRound(Long certificateId, ExamType examType, String examRound);

}
