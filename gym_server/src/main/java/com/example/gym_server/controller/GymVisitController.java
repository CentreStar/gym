package com.example.gym_server.controller;

import com.example.gym_server.constant.ErrorCode;
import com.example.gym_server.dto.ApiResponse;
import com.example.gym_server.dto.MonthlyAttendanceResponse;
import com.example.gym_server.dto.ScanRequest;
import com.example.gym_server.dto.VisitResponse;
import com.example.gym_server.entity.User;
import com.example.gym_server.exception.BusinessException;
import com.example.gym_server.service.GymVisitService;
import com.example.gym_server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/attendance")
public class GymVisitController {
    private final GymVisitService service;
    private final UserService users;

    public GymVisitController(GymVisitService service, UserService users) {
        this.service = service; this.users = users;
    }

    @PostMapping("/scan")
    public ApiResponse<VisitResponse> scan(@Valid @RequestBody ScanRequest request) {
        return ApiResponse.ok(service.scan(request.getQrCode()));
    }

    @GetMapping("/monthly/{username}")
    public ApiResponse<MonthlyAttendanceResponse> monthly(@PathVariable String username,
                                                          @RequestParam(required = false) Integer year,
                                                          @RequestParam(required = false) Integer month) {
        User user = users.findByUsername(username);
        if (user == null) throw BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        YearMonth current = YearMonth.now();
        return ApiResponse.ok(service.monthly(user.getId(),
                year == null ? current.getYear() : year,
                month == null ? current.getMonthValue() : month));
    }

    @GetMapping("/today")
    public ApiResponse<Map<String, Object>> today() {
        return ApiResponse.ok(Map.of("count", service.todayCount()));
    }
}
