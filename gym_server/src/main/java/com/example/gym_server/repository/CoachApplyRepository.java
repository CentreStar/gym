package com.example.gym_server.repository;


import com.example.gym_server.entity.CoachApply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface CoachApplyRepository
        extends JpaRepository<CoachApply, Long> {
    Optional<CoachApply> findTopByUserIdOrderByCreateTimeDesc(Long userId);


}
