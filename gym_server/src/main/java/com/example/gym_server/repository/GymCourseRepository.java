package com.example.gym_server.repository;
import com.example.gym_server.entity.GymCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface GymCourseRepository extends JpaRepository<GymCourse, Long> { List<GymCourse> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime time); }
