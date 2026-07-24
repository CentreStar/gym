package com.example.gym_server.service;


import com.example.gym_server.entity.CoachInfo;
import com.example.gym_server.repository.CoachInfoRepository;

import org.springframework.stereotype.Service;


import java.time.LocalDateTime;



@Service
public class CoachInfoService {



    private final CoachInfoRepository coachInfoRepository;



    public CoachInfoService(
            CoachInfoRepository coachInfoRepository
    ){

        this.coachInfoRepository = coachInfoRepository;

    }




    // 创建教练资料

    public CoachInfo create(
            CoachInfo coachInfo
    ){


        coachInfo.setCreateTime(
                LocalDateTime.now()
        );


        return coachInfoRepository.save(coachInfo);

    }





    // 查询教练资料

    public CoachInfo getByUserId(
            Long userId
    ){


        return coachInfoRepository
                .findByUserId(userId)
                .orElse(null);

    }



}