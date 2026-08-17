package com.example.gym_server.repository;
import com.example.gym_server.entity.RefundApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RefundApplicationRepository extends JpaRepository<RefundApplication, Long> {
    List<RefundApplication> findByStatusOrderByCreateTimeDesc(String status);
    List<RefundApplication> findAllByOrderByCreateTimeDesc();
}
