package com.example.gym_server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_orders")
public class GymOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long userId;
    private Long cardId;
    private String type;
    private String title;
    private BigDecimal amount;
    private String status = "PAID";
    private LocalDateTime createTime = LocalDateTime.now();
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public Long getCardId() { return cardId; } public void setCardId(Long cardId) { this.cardId = cardId; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; } public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
