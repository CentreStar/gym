package com.example.gym_server.service;


import com.example.gym_server.entity.CoachApply;
import com.example.gym_server.entity.User;
import com.example.gym_server.repository.CoachApplyRepository;
import com.example.gym_server.repository.UserRepository;
import com.example.gym_server.entity.CoachInfo;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;



@Service
public class CoachApplyService {


    private final CoachApplyRepository coachApplyRepository;

    private final UserRepository userRepository;

    private final CoachInfoService coachInfoService;


    public CoachApplyService(
            CoachApplyRepository coachApplyRepository,
            UserRepository userRepository,
            CoachInfoService coachInfoService
    ){

        this.coachApplyRepository = coachApplyRepository;
        this.userRepository = userRepository;
        this.coachInfoService = coachInfoService;

    }





    // 用户提交申请
    public CoachApply apply(
            CoachApply coachApply
    ){

        coachApply.setStatus("PENDING");

        coachApply.setCreateTime(
                LocalDateTime.now()
        );


        return coachApplyRepository.save(coachApply);

    }





    // 管理员查看所有申请
    public List<CoachApply> getAllApply(){


        return coachApplyRepository.findAll();

    }





    // 管理员通过申请
    public void pass(Long id){


        CoachApply apply =
                coachApplyRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException("申请不存在")
                        );



        // 修改申请状态
        apply.setStatus("APPROVED");


        coachApplyRepository.save(apply);





        // 修改用户角色
        User user =
                userRepository.findById(
                                apply.getUserId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException("用户不存在")
                        );



        user.setRole("COACH");


        userRepository.save(user);


        // 创建教练资料

        CoachInfo coachInfo = new CoachInfo();


        coachInfo.setUserId(
                apply.getUserId()
        );


        coachInfo.setName(
                apply.getName()
        );


        coachInfo.setIntroduction(
                apply.getDescription()
        );


        coachInfo.setCreateTime(
                LocalDateTime.now()
        );


        coachInfoService.create(coachInfo);

    }





    // 管理员拒绝申请
    public void reject(Long id){


        CoachApply apply =
                coachApplyRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException("申请不存在")
                        );


        apply.setStatus("REJECT");


        coachApplyRepository.save(apply);


    }



}
