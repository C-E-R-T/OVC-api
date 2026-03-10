package com.example.ovcbackend.schedule.service;

import com.example.ovcbackend.schedule.dto.CalenderResponse;
import com.example.ovcbackend.schedule.entity.Schedule;

import java.util.List;

public interface ScheduleService {
    List<CalenderResponse> getMonthlyCalender(int year, int month);
    List<Schedule> getTest();
}
