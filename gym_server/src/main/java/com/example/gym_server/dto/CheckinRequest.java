package com.example.gym_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 课程签到：教练扫描会员二维码。 */
public class CheckinRequest {
    @NotBlank
    private String qrCode;
    @NotNull
    private Long courseId;

    public String getQrCode() { return qrCode; } public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public Long getCourseId() { return courseId; } public void setCourseId(Long courseId) { this.courseId = courseId; }
}
