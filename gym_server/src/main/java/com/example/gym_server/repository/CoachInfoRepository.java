package com.example.gym_server.repository;


import com.example.gym_server.entity.CoachInfo;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;



public interface CoachInfoRepository
        extends JpaRepository<CoachInfo,Long> {


    Optional<CoachInfo> findByUserId(Long userId);


}