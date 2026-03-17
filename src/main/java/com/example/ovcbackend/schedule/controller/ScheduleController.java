package com.example.ovcbackend.schedule.controller;

import com.example.ovcbackend.global.commonResponse.OkResponse;
import com.example.ovcbackend.schedule.dto.CalenderResponse;
import com.example.ovcbackend.schedule.dto.ScheduleResponse;
import com.example.ovcbackend.schedule.service.ScheduleService;
import com.example.ovcbackend.xml.sync.service.ScheduleSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;

@Tag(name = "calendar/schedule Controller", description = "calendar/schedule 관련 api")
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final ScheduleSyncService scheduleSyncService;

    // 월 단위 캘린더 이벤트 조회
    @Operation(summary = "월 단위 캘린더 조회", description = "특정 연도와 월에 해당하는 모든 자격증 시험 일정을 조회합니다.")
    @GetMapping
    public ResponseEntity<OkResponse<List<CalenderResponse>>> getMonthlyCalendar(
            @Parameter(description = "조회 연도")
            @RequestParam(name="year") int year,
            @Parameter(description = "조회 월")
            @RequestParam(name="month") int month,
            HttpServletRequest request
    ){
        List<CalenderResponse> responses = scheduleService.getMonthlyCalender(year, month);

        return ResponseEntity.ok(OkResponse.success(responses, request.getRequestURI()));
    }

    // 지정 자격증의 올해 일정을 수동 동기화
    @Operation(
            summary = "시험 일정 동기화",
            description = "공공데이터 포털 API를 호출하여 지정된 자격증들의 올해 시험 일정을 불러옵니다."
    )
    @PostMapping("/sync")
    public ResponseEntity<OkResponse<List<String>>> syncSchedules(
            @RequestParam(name = "certIds") List<String> certIds,
            HttpServletRequest request
    ) {
        List<String> syncedCertIds = scheduleSyncService.syncCurrentYearSchedules(certIds);
        return ResponseEntity.ok(OkResponse.success("시험일정 동기화가 완료되었습니다.", syncedCertIds, request.getRequestURI()));
    }
    @Operation(summary = "자격증별 상세 일정 조회", description = "특정 자격증의 연도별 시험 일정을 상세 조회합니다. 연도 미입력 시 올해가 기준")
    @GetMapping("/certificate/{certId}")
    public ResponseEntity<OkResponse<List<ScheduleResponse>>> getCertificateSchedules(
            @Parameter(description = "자격증 id")
            @PathVariable("certId") Long certId,
            @Parameter(description = "조회 연도(기본값: 현재 연도)")
            @RequestParam(value = "year", required = false) Integer year,
            HttpServletRequest request
    ) {
        if(year == null) {
            year = Year.now().getValue();
        }
        List<ScheduleResponse> schedules = scheduleService.getSchedules(certId, year);

        return ResponseEntity.ok(OkResponse.success(schedules, request.getRequestURI()));
    }
}
