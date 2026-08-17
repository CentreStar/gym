package com.example.gym_server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_courses")
public class GymCourse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    private String type;
    private String coachName;
    private LocalDateTime startTime;
    private Integer capacity;
    private BigDecimal price;
    private String description;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public String getCoachName() { return coachName; } public void setCoachName(String coachName) { this.coachName = coachName; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public Integer getCapacity() { return capacity; } public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
}
