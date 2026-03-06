package com.example.ovcbackend.schedule.service;

import com.example.ovcbackend.schedule.dto.CalenderResponse;
import com.example.ovcbackend.schedule.entity.Schedule;
import com.example.ovcbackend.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Override
    public List<CalenderResponse> getMonthlyCalender(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // DB 조회
        List<Schedule> schedules = scheduleRepository.findAllByMonth(start, end);
        // response를 위한 arraylist
        List<CalenderResponse> events = new ArrayList<>();

        for (Schedule s : schedules) {
            // 접수일정
            events.add(convertToDto(s, "APPLY", s.getApplyStartAt(), s.getApplyEndAt()));
            // 시험일정
            events.add(convertToDto(s, "EXAM", s.getExamAt(), s.getExamAt()));
            // 시험결과일정
            events.add(convertToDto(s, "RESULT", s.getResultAt(), s.getResultAt()));
        }

        return events;

    }

    // type에 따라 dto로 변환하기 위해
    private CalenderResponse convertToDto(Schedule s, String type, LocalDateTime start, LocalDateTime end){
        return CalenderResponse.builder()
                .scheduleId(s.getId())
                .certificateName(s.getCertificate().getName())
                .examName(s.getExamName())
                .examType(s.getExamType())
                .eventType(type)
                .startDate(start)
                .endDate(end)
                .build();
    }
}
