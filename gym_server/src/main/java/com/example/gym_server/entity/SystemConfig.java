package com.example.gym_server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统配置项（system_configs），key-value 结构。
 */
@Entity
@Table(name = "system_configs")
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String configKey;
    @Column(nullable = false, length = 500)
    private String configValue;
    private String description;
    private LocalDateTime createTime = LocalDateTime.now();

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getConfigKey() { return configKey; } public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; } public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; } public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
