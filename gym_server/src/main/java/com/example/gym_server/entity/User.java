package com.example.gym_server.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;


@Entity
@Table(name = "users")
public class User {


    // 主键
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // 用户名
    private String username;


    // 密码
    private String password;


    // 手机号
    private String phone;


    // 用户角色
    private String role;


    // 用户状态
    private String status;


    // 创建时间
    private LocalDateTime createTime;



    // 无参构造方法
    public User() {

    }



    // getter 和 setter


    public Long getId() {

        return id;

    }


    public void setId(Long id) {

        this.id = id;

    }



    public String getUsername() {

        return username;

    }


    public void setUsername(String username) {

        this.username = username;

    }



    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {

        return password;

    }


    public void setPassword(String password) {

        this.password = password;

    }



    public String getPhone() {

        return phone;

    }


    public void setPhone(String phone) {

        this.phone = phone;

    }



    public String getRole() {

        return role;

    }


    public void setRole(String role) {

        this.role = role;

    }



    public String getStatus() {

        return status;

    }


    public void setStatus(String status) {

        this.status = status;

    }



    public LocalDateTime getCreateTime() {

        return createTime;

    }


    public void setCreateTime(LocalDateTime createTime) {

        this.createTime = createTime;

    }

}