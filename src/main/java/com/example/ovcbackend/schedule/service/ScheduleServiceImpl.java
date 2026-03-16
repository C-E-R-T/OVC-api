package com.example.ovcbackend.schedule.service;

import com.example.ovcbackend.schedule.dto.CalenderResponse;
import com.example.ovcbackend.schedule.dto.ScheduleResponse;
import com.example.ovcbackend.schedule.entity.Schedule;
import com.example.ovcbackend.schedule.exception.ScheduleNotFoundException;
import com.example.ovcbackend.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;

    // 지정 월 범위의 일정을 조회해 이벤트 타입별(APPLY/EXAM/RESULT)로 펼쳐 반환
    @Override
    public List<CalenderResponse> getMonthlyCalender(int year, int month) {
        log.info("[/api/calender] 캘린더 조회 시작 - 연도: {}, 월: {}", year, month);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        // DB 조회
        List<Schedule> schedules = scheduleRepository.findAllByMonth(start, end);
        // response를 위한 arraylist
        List<CalenderResponse> events = new ArrayList<>();
        if(schedules.isEmpty()) {
            log.warn("[/api/calender] 해당 기간에 등록된 일정이 없습니다. - 연도: {}, 월: {})", year, month);
        }
        for (Schedule s : schedules) {
            // 접수일정
            events.add(convertToDto(s, "APPLY", s.getApplyStartAt(), s.getApplyEndAt()));
            // 시험일정
            events.add(convertToDto(s, "EXAM", s.getExamStartAt(), s.getExamEndAt()));
            // 시험결과일정
            events.add(convertToDto(s, "RESULT", s.getResultAt(), s.getResultAt()));
        }

        return events;

    }


    // type에 따라 dto로 변환하기 위해
    private CalenderResponse convertToDto(Schedule s, String type, LocalDateTime start, LocalDateTime end){
        // 일정의 시작과 끝의 기간 차 (달력에 기간 길이 때 사용)
        long duration = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        return CalenderResponse.builder()
                .scheduleId(s.getId())
                .certificateName(s.getCertificate().getName())
                .examName(s.getExamName())
                .examType(s.getExamType())
                .eventType(type)
                .startDate(start)
                .endDate(end)
                .durationDays(duration)
                .certId(s.getCertificate().getId())
                .build();
    }


    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(Long certId, int year) {
        log.info("[calendar/certificate/{certId}/?{year}] 자격증 연간 일정 조회 - certId: {}, Year: {} ", certId, year);
        // 이쪽도 만약에 일정이 존재하지 않으면 어떻게 하는게 좋을까 특정 id를 조회하는 거니까 exception 처리가 맞는 것 같다.
        // 그럼 변수 하나 새로 만들어야 될 듯
        List<Schedule> schedules = scheduleRepository.findByCertificateIdAndYear(certId, year);

        if(schedules.isEmpty()) {
            log.error("[calendar/certificate/{certId}/?{year}] 일정을 찾을 수 없음 - certId: {}, Year: {}", certId, year);
            throw new ScheduleNotFoundException(year + "년도에 해당하는 자격증 일정이 등록되지 않았습니다.");
        }
        return schedules.stream()
                .map(ScheduleResponse::from)
                .toList();
    }
}
