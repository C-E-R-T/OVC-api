package com.example.ovcbackend.schedule.service;

import com.example.ovcbackend.schedule.dto.CalenderResponse;
import com.example.ovcbackend.schedule.dto.ScheduleResponse;
import java.util.List;

public interface ScheduleService {
    List<CalenderResponse> getMonthlyCalender(int year, int month);
//    List<Schedule> getTest();

    List<ScheduleResponse> getSchedules(Long certId, int year);
}
