package com.example.gym_server.dto;
import jakarta.validation.constraints.NotBlank;
public class ScanRequest { @NotBlank private String qrCode; public String getQrCode() { return qrCode; } public void setQrCode(String qrCode) { this.qrCode = qrCode; } }
