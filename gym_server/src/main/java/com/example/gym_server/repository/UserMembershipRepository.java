package com.example.gym_server.repository;
import com.example.gym_server.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    boolean existsByUserIdAndStatusAndEndDateAfter(Long userId, String status, LocalDateTime now);
    Optional<UserMembership> findFirstByUserIdAndStatusOrderByEndDateDesc(Long userId, String status);
    Optional<UserMembership> findByOrderId(Long orderId);
    List<UserMembership> findByUserIdOrderByStartDateDesc(Long userId);
}
