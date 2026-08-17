package com.example.gym_server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户会员权益记录（user_memberships）。
 * 购买成功后生成；入场时校验是否存在 ACTIVE 且未过期的记录。
 */
@Entity
@Table(name = "user_memberships",
        indexes = {
                @Index(name = "idx_user_membership_user", columnList = "userId"),
                @Index(name = "idx_user_membership_status", columnList = "userId, status")
        })
public class UserMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long cardId;
    @Column(nullable = false)
    private Long orderId;
    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = false)
    private LocalDateTime endDate;
    private String status = "ACTIVE";
    private LocalDateTime createTime = LocalDateTime.now();

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public Long getCardId() { return cardId; } public void setCardId(Long cardId) { this.cardId = cardId; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long orderId) { this.orderId = orderId; }
    public LocalDateTime getStartDate() { return startDate; } public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; } public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; } public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
