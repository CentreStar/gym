package com.example.gym_server.repository;
import com.example.gym_server.entity.GymOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface GymOrderRepository extends JpaRepository<GymOrder, Long> {
    List<GymOrder> findByUserIdOrderByCreateTimeDesc(Long userId);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM GymOrder o WHERE o.createTime >= :start AND o.createTime < :end AND o.status <> 'REFUNDED'")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
