package com.example.gym_server.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record RefundRequest(@NotBlank String username, @NotNull Long orderId, @NotBlank String reason, String description) { }
