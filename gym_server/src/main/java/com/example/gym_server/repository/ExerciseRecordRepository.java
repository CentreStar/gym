package com.example.gym_server.repository;
import com.example.gym_server.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> { List<ExerciseRecord> findByUserIdOrderByCreateTimeDesc(Long userId); }
