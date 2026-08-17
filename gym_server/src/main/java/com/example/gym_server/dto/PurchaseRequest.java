package com.example.gym_server.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record PurchaseRequest(@NotBlank String username, @NotNull Long cardId) { }
