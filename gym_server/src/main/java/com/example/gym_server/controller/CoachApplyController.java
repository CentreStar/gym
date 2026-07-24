package com.example.gym_server.controller;


import com.example.gym_server.entity.CoachApply;
import com.example.gym_server.service.CoachApplyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/coach")
public class CoachApplyController {


    private final CoachApplyService coachApplyService;



    public CoachApplyController(
            CoachApplyService coachApplyService
    ){

        this.coachApplyService = coachApplyService;

    }



    // 用户申请成为教练
    @PostMapping("/apply")
    public CoachApply apply(
            @RequestBody CoachApply coachApply
    ){

        return coachApplyService.apply(coachApply);

    }





    // 管理员查看教练申请列表
    @GetMapping("/admin/apply/list")
    public List<CoachApply> getApplyList(){

        return coachApplyService.getAllApply();

    }





    // 管理员通过申请
    @PutMapping("/admin/apply/pass/{id}")
    public String pass(
            @PathVariable Long id
    ){

        coachApplyService.pass(id);

        return "审核通过";

    }





    // 管理员拒绝申请
    @PutMapping("/admin/apply/reject/{id}")
    public String reject(
            @PathVariable Long id
    ){

        coachApplyService.reject(id);

        return "已拒绝";

    }


}