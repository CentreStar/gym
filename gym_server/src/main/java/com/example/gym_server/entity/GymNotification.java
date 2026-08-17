package com.example.gym_server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_notifications")
public class GymNotification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String title;
    @Column(length = 2000) private String content;
    private String targetRole = "ALL";
    /** 定向用户 ID 列表，逗号分隔；为空表示该角色下所有人可见。 */
    @Column(length = 1000)
    private String targetUserIds;
    private LocalDateTime createTime = LocalDateTime.now();
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public String getTargetRole() { return targetRole; } public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getTargetUserIds() { return targetUserIds; } public void setTargetUserIds(String targetUserIds) { this.targetUserIds = targetUserIds; }
    public LocalDateTime getCreateTime() { return createTime; } public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
