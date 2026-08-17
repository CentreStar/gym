package com.example.gym_server.repository;
import com.example.gym_server.entity.CourseBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CourseBookingRepository extends JpaRepository<CourseBooking, Long> {
    List<CourseBooking> findByUserIdAndStatus(Long userId, String status);
    long countByCourseIdAndStatus(Long courseId, String status);
    Optional<CourseBooking> findByUserIdAndCourseId(Long userId, Long courseId);
    List<CourseBooking> findByCourseIdAndStatusOrderByCreateTimeAsc(Long courseId, String status);

    @Query("SELECT c.courseId, COUNT(c) FROM CourseBooking c WHERE c.status = 'BOOKED' GROUP BY c.courseId ORDER BY COUNT(c) DESC")
    List<Object[]> countBookingsGroupByCourse();
}
