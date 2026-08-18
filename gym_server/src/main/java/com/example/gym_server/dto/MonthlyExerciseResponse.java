package com.example.gym_server.dto;

import java.util.List;

/** 月度运动统计：只依据 exercise_records.exercise_date。 */
public record MonthlyExerciseResponse(int year, int month, long exerciseDays, long totalMinutes, List<String> exerciseDates) { }
