package com.example.gym_server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_applications")
public class RefundApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long userId;
    private Long orderId;
    private String cardName;
    private BigDecimal refundAmount;
    private String reason;
    @Column(length = 500)
    private String description;
    private String rejectReason;
    private String status = "PENDING";
    private LocalDateTime createTime = LocalDateTime.now();
    public Long getId() { return id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getCardName() { return cardName; } public void setCardName(String cardName) { this.cardName = cardName; }
    public BigDecimal getRefundAmount() { return refundAmount; } public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getRejectReason() { return rejectReason; } public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
}
