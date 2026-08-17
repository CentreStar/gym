package com.example.gym_server.repository;
import com.example.gym_server.entity.PrivateBooking; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface PrivateBookingRepository extends JpaRepository<PrivateBooking,Long>{ List<PrivateBooking> findByUsernameOrderByCreateTimeDesc(String username); }
