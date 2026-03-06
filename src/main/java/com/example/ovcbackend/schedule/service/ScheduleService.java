package com.example.ovcbackend.schedule.service;

import com.example.ovcbackend.schedule.dto.CalenderResponse;

import java.util.List;

public interface ScheduleService {
    List<CalenderResponse> getMonthlyCalender(int year, int month);
}
