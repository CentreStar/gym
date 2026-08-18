package com.example.gym_server.repository;

import com.example.gym_server.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {

    List<ExerciseRecord> findByUserIdOrderByExerciseDateDesc(Long userId);

    List<ExerciseRecord> findByUserIdAndExerciseDateBetweenOrderByExerciseDateDesc(Long userId, LocalDate start, LocalDate end);

    /** 同一用户去重后的运动天数（依据 exercise_date）。 */
    @Query("select count(distinct e.exerciseDate) from ExerciseRecord e where e.userId = :userId and e.exerciseDate >= :start and e.exerciseDate < :end")
    long countDistinctExerciseDays(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    /** 累计运动时长（分钟）。 */
    @Query("select coalesce(sum(e.durationMinutes), 0) from ExerciseRecord e where e.userId = :userId and e.exerciseDate >= :start and e.exerciseDate < :end")
    long sumDurationMinutes(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    /** 有运动记录的日期列表（去重、升序）。 */
    @Query("select distinct e.exerciseDate from ExerciseRecord e where e.userId = :userId and e.exerciseDate >= :start and e.exerciseDate < :end order by e.exerciseDate")
    List<LocalDate> distinctExerciseDates(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
