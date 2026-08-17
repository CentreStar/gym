package com.example.gym_server.dto;

import com.example.gym_server.entity.GymCourse;

public record CourseDetailResponse(GymCourse course, long bookedCount, long remaining) { }
