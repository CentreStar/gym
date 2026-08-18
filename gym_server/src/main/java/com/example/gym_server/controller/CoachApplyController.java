package com.example.gym_server.controller;

import com.example.gym_server.dto.ApiResponse;
import com.example.gym_server.entity.CoachApply;
import com.example.gym_server.service.CoachApplyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coach")
@CrossOrigin
public class CoachApplyController {

    private final CoachApplyService coachApplyService;

    public CoachApplyController(CoachApplyService coachApplyService) {
        this.coachApplyService = coachApplyService;
    }

    // 用户申请成为教练
    @PostMapping("/apply")
    public ApiResponse<CoachApply> apply(@RequestBody CoachApply coachApply) {
        return ApiResponse.ok(coachApplyService.apply(coachApply));
    }

    @GetMapping("/apply/user/{userId}")
    public ApiResponse<CoachApply> getUserApply(@PathVariable Long userId) {
        return ApiResponse.ok(coachApplyService.getLatest(userId));
    }

    // 管理员查看教练申请列表
    @GetMapping("/admin/apply/list")
    public ApiResponse<List<CoachApply>> getApplyList() {
        return ApiResponse.ok(coachApplyService.getAllApply());
    }

    // 管理员通过申请
    @PutMapping("/admin/apply/pass/{id}")
    public ApiResponse<Void> pass(@PathVariable Long id) {
        coachApplyService.pass(id);
        return ApiResponse.ok();
    }

    // 管理员拒绝申请
    @PutMapping("/admin/apply/reject/{id}")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        coachApplyService.reject(id, reason);
        return ApiResponse.ok();
    }
}
