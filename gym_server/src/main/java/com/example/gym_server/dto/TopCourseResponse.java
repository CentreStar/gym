package com.example.gym_server.dto;

public record TopCourseResponse(Long courseId, String title, String type, long bookings) { }
