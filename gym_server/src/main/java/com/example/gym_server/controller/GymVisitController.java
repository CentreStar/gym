package com.example.gym_server.controller;

import com.example.gym_server.dto.ApiResponse;
import com.example.gym_server.dto.ScanRequest;
import com.example.gym_server.dto.VisitResponse;
import com.example.gym_server.service.GymVisitService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 签到/进出场模块：只负责二维码扫码开门以及进出场记录。 */
@RestController
@CrossOrigin
@RequestMapping("/api/v1/attendance")
public class GymVisitController {
    private final GymVisitService service;

    public GymVisitController(GymVisitService service) {
        this.service = service;
    }

    @PostMapping("/scan")
    public ApiResponse<VisitResponse> scan(@Valid @RequestBody ScanRequest request) {
        return ApiResponse.ok(service.scan(request.getQrCode()));
    }

    @GetMapping("/today")
    public ApiResponse<Map<String, Object>> today() {
        return ApiResponse.ok(Map.of("count", service.todayCount()));
    }
}
