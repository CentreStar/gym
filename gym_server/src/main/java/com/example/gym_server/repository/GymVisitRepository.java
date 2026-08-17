package com.example.gym_server.repository;
import com.example.gym_server.entity.GymVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GymVisitRepository extends JpaRepository<GymVisit, Long> {
    Optional<GymVisit> findFirstByUserIdAndExitedAtIsNullOrderByEnteredAtDesc(Long userId);
    List<GymVisit> findByUserIdAndEnteredAtGreaterThanEqualAndEnteredAtLessThan(Long userId, LocalDateTime start, LocalDateTime end);
    long countByEnteredAtBetween(LocalDateTime start, LocalDateTime end);
    List<GymVisit> findByEnteredAtGreaterThanEqual(LocalDateTime start);
}
