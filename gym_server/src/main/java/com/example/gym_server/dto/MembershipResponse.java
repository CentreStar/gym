package com.example.gym_server.dto;

import java.time.LocalDateTime;

public record MembershipResponse(
        Long id,
        Long cardId,
        String cardName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status) {
}
