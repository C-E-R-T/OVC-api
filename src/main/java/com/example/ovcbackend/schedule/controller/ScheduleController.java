package com.example.ovcbackend.schedule.controller;

import com.example.ovcbackend.global.commonResponse.OkResponse;
import com.example.ovcbackend.schedule.dto.CalenderResponse;
import com.example.ovcbackend.schedule.service.ScheduleService;
import com.example.ovcbackend.xml.sync.service.ScheduleSyncService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "calendar/schedule Controller", description = "calendar/schedule 관련 api")
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final ScheduleSyncService scheduleSyncService;

//    @GetMapping("/test")
//    public ResponseEntity<List<Schedule>> getTest(){
//        return ResponseEntity.ok(scheduleService.getTest());
//    }

    @GetMapping
    public ResponseEntity<OkResponse<List<CalenderResponse>>> getMonthlyCalendar(
            @RequestParam(name="year") int year,
            @RequestParam(name="month") int month,
            HttpServletRequest request
    ){
        List<CalenderResponse> responses = scheduleService.getMonthlyCalender(year, month);

        return ResponseEntity.ok(OkResponse.success(responses, request.getRequestURI()));
    }

    @PostMapping("/sync")
    public ResponseEntity<OkResponse<List<String>>> syncSchedules(
            @RequestParam(name = "certIds") List<String> certIds,
            HttpServletRequest request
    ) {
        List<String> syncedCertIds = scheduleSyncService.syncCurrentYearSchedules(certIds);
        return ResponseEntity.ok(OkResponse.success("시험일정 동기화가 완료되었습니다.", syncedCertIds, request.getRequestURI()));
    }
}
