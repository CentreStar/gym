package com.example.gym_server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_visits", indexes = @Index(name = "idx_gym_visit_user_entered", columnList = "userId, enteredAt"))
public class GymVisit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private LocalDateTime enteredAt;
    private LocalDateTime exitedAt;
    private Long durationSeconds;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getEnteredAt() { return enteredAt; } public void setEnteredAt(LocalDateTime enteredAt) { this.enteredAt = enteredAt; }
    public LocalDateTime getExitedAt() { return exitedAt; } public void setExitedAt(LocalDateTime exitedAt) { this.exitedAt = exitedAt; }
    public Long getDurationSeconds() { return durationSeconds; } public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
}
