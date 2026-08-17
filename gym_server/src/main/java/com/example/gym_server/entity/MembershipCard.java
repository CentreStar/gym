package com.example.gym_server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "membership_cards")
public class MembershipCard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    private Integer validDays;
    private BigDecimal price;
    private String description;
    private boolean enabled = true;
    private LocalDateTime createTime = LocalDateTime.now();
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Integer getValidDays() { return validDays; } public void setValidDays(Integer validDays) { this.validDays = validDays; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public boolean isEnabled() { return enabled; } public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreateTime() { return createTime; } public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
