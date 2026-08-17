package com.example.gym_server.controller;

import com.example.gym_server.dto.ApiResponse;
import com.example.gym_server.entity.CoachInfo;
import com.example.gym_server.service.CoachInfoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coach/info")
@CrossOrigin
public class CoachInfoController {

    private final CoachInfoService coachInfoService;

    public CoachInfoController(CoachInfoService coachInfoService) {
        this.coachInfoService = coachInfoService;
    }

    // 创建教练资料
    @PostMapping("/create")
    public ApiResponse<CoachInfo> create(@RequestBody CoachInfo coachInfo) {
        return ApiResponse.ok(coachInfoService.create(coachInfo));
    }

    // 查询教练资料
    @GetMapping("/{userId}")
    public ApiResponse<CoachInfo> get(@PathVariable Long userId) {
        return ApiResponse.ok(coachInfoService.getByUserId(userId));
    }
}
