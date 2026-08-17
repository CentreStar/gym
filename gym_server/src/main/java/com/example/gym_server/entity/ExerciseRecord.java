package com.example.gym_server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_records")
public class ExerciseRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long userId;
    private String bodyPart;
    private String exerciseType;
    private String actionName;
    private Double weight;
    private Integer sets;
    private Integer durationMinutes;
    private Double speed;
    private Double incline;
    private Double calories;
    private LocalDateTime createTime = LocalDateTime.now();
    public Long getId() { return id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public String getBodyPart() { return bodyPart; } public void setBodyPart(String bodyPart) { this.bodyPart = bodyPart; }
    public String getExerciseType() { return exerciseType; } public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }
    public String getActionName() { return actionName; } public void setActionName(String actionName) { this.actionName = actionName; }
    public Double getWeight() { return weight; } public void setWeight(Double weight) { this.weight = weight; }
    public Integer getSets() { return sets; } public void setSets(Integer sets) { this.sets = sets; }
    public Integer getDurationMinutes() { return durationMinutes; } public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Double getSpeed() { return speed; } public void setSpeed(Double speed) { this.speed = speed; }
    public Double getIncline() { return incline; } public void setIncline(Double incline) { this.incline = incline; }
    public Double getCalories() { return calories; } public void setCalories(Double calories) { this.calories = calories; }
    public LocalDateTime getCreateTime() { return createTime; }
}
