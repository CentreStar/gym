package com.example.gym_server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_bookings", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "courseId"}))
public class CourseBooking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long userId;
    private Long courseId;
    private String status = "BOOKED";
    private LocalDateTime createTime = LocalDateTime.now();
    public Long getId() { return id; } public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public Long getCourseId() { return courseId; } public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
}
