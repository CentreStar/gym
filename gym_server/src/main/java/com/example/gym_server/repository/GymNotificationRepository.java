package com.example.gym_server.repository;
import com.example.gym_server.entity.GymNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GymNotificationRepository extends JpaRepository<GymNotification, Long> { List<GymNotification> findByTargetRoleInOrderByCreateTimeDesc(List<String> roles); }
