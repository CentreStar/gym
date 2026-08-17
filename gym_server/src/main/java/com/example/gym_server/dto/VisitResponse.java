package com.example.gym_server.dto;
import java.time.LocalDateTime;
public record VisitResponse(String action, Long visitId, LocalDateTime time, Long durationSeconds) { }
