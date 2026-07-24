package com.example.gym_server.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "coach_info")
public class CoachInfo {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    // 对应用户id
    @Column(unique = true)
    private Long userId;



    // 教练姓名
    private String name;



    // 教练头像
    private String avatar;



    // 教练介绍
    private String introduction;



    // 擅长方向
    private String specialty;



    // 创建时间
    private LocalDateTime createTime;



    public CoachInfo(){

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



    public String getAvatar() {
        return avatar;
    }


    public void setAvatar(String avatar) {
        this.avatar=avatar;
    }



    public String getIntroduction() {
        return introduction;
    }


    public void setIntroduction(String introduction) {
        this.introduction=introduction;
    }



    public String getSpecialty() {
        return specialty;
    }


    public void setSpecialty(String specialty) {
        this.specialty=specialty;
    }



    public LocalDateTime getCreateTime() {
        return createTime;
    }


    public void setCreateTime(LocalDateTime createTime) {
        this.createTime=createTime;
    }


}