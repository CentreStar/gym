package com.example.gym_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * 添加/编辑运动记录的请求体。
 * bodyParts 以数组形式提交（力量训练部位多选），服务层拼接为逗号分隔字符串。
 */
public class ExerciseRequest {
    @NotBlank
    private String exerciseType;
    private List<String> bodyParts;
    private String actionName;
    private Double weight;
    private Integer sets;
    private String handleType;
    private Integer resistance;
    private Double speed;
    private Double incline;
    @NotNull
    private Integer durationMinutes;
    private LocalDate exerciseDate;

    public String getExerciseType() { return exerciseType; } public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }
    public List<String> getBodyParts() { return bodyParts; } public void setBodyParts(List<String> bodyParts) { this.bodyParts = bodyParts; }
    public String getActionName() { return actionName; } public void setActionName(String actionName) { this.actionName = actionName; }
    public Double getWeight() { return weight; } public void setWeight(Double weight) { this.weight = weight; }
    public Integer getSets() { return sets; } public void setSets(Integer sets) { this.sets = sets; }
    public String getHandleType() { return handleType; } public void setHandleType(String handleType) { this.handleType = handleType; }
    public Integer getResistance() { return resistance; } public void setResistance(Integer resistance) { this.resistance = resistance; }
    public Double getSpeed() { return speed; } public void setSpeed(Double speed) { this.speed = speed; }
    public Double getIncline() { return incline; } public void setIncline(Double incline) { this.incline = incline; }
    public Integer getDurationMinutes() { return durationMinutes; } public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalDate getExerciseDate() { return exerciseDate; } public void setExerciseDate(LocalDate exerciseDate) { this.exerciseDate = exerciseDate; }
}
