package com.example.gym_server.dto;
import java.util.List;
public record MonthlyAttendanceResponse(int year, int month, long activeDays, long totalDurationSeconds, List<Integer> activeDayNumbers) { }
