package com.example.gym_server.repository;
import com.example.gym_server.entity.CoachAvailability; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface CoachAvailabilityRepository extends JpaRepository<CoachAvailability,Long>{ List<CoachAvailability> findByStatusOrderByStartTimeAsc(String status); }
