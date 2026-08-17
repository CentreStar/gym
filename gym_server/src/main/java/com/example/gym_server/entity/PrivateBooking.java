package com.example.gym_server.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="private_bookings")
public class PrivateBooking {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long availabilityId; private Long userId; private String username; private String status="PENDING"; private String rejectReason; private LocalDateTime createTime=LocalDateTime.now();
    public Long getId(){return id;}
    public Long getAvailabilityId(){return availabilityId;} public void setAvailabilityId(Long v){availabilityId=v;}
    public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getRejectReason(){return rejectReason;} public void setRejectReason(String v){rejectReason=v;}
    public LocalDateTime getCreateTime(){return createTime;}
}
