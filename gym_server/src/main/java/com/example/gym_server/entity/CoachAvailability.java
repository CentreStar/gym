package com.example.gym_server.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="coach_availability")
public class CoachAvailability {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long coachId; private String coachName; private LocalDateTime startTime; private LocalDateTime endTime; private String status="OPEN";
 public Long getId(){return id;} public void setId(Long v){id=v;} public Long getCoachId(){return coachId;} public void setCoachId(Long v){coachId=v;} public String getCoachName(){return coachName;} public void setCoachName(String v){coachName=v;} public LocalDateTime getStartTime(){return startTime;} public void setStartTime(LocalDateTime v){startTime=v;} public LocalDateTime getEndTime(){return endTime;} public void setEndTime(LocalDateTime v){endTime=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
