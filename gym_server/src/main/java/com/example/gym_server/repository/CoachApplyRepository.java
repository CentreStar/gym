package com.example.gym_server.repository;


import com.example.gym_server.entity.CoachApply;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CoachApplyRepository
        extends JpaRepository<CoachApply, Long> {


}