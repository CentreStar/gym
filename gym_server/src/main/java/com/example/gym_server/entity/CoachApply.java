package com.example.gym_server.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name="coach_apply")
public class CoachApply {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    //申请用户id
    private Long userId;


    //教练姓名
    private String name;


    //联系方式
    private String phone;


    //个人介绍
    private String description;


    //审核状态
    private String status;


    //申请时间
    private LocalDateTime createTime;



    public CoachApply(){

    }



    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id=id;
    }



    public Long getUserId() {
        return userId;
    }


    public void setUserId(Long userId) {
        this.userId=userId;
    }



    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name=name;
    }



    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone=phone;
    }



    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description=description;
    }



    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status=status;
    }



    public LocalDateTime getCreateTime() {
        return createTime;
    }


    public void setCreateTime(LocalDateTime createTime) {
        this.createTime=createTime;
    }

}