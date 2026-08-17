package com.example.gym_server.dto;
import com.example.gym_server.entity.GymCourse;
public record BookingResponse(Long bookingId, String status, GymCourse course) { }
