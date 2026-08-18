package com.example.gym_server.controller;

import com.example.gym_server.dto.*;
import com.example.gym_server.entity.*;
import com.example.gym_server.repository.CoachAvailabilityRepository;
import com.example.gym_server.repository.PrivateBookingRepository;
import com.example.gym_server.repository.UserRepository;
import com.example.gym_server.service.GymBusinessService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class GymBusinessController {
    private final GymBusinessService service;
    private final CoachAvailabilityRepository availabilities;
    private final PrivateBookingRepository privateBookings;
    private final UserRepository users;

    public GymBusinessController(GymBusinessService service,
                                 CoachAvailabilityRepository availabilities, PrivateBookingRepository privateBookings,
                                 UserRepository users) {
        this.service = service;
        this.availabilities = availabilities; this.privateBookings = privateBookings; this.users = users;
    }

    // ==================== 教练空闲时间 / 私教预约 ====================
    @GetMapping("/coach/availability")
    public ApiResponse<List<CoachAvailability>> availability() {
        return ApiResponse.ok(availabilities.findByStatusOrderByStartTimeAsc("OPEN"));
    }

    @PostMapping("/coach/{username}/availability")
    public ApiResponse<CoachAvailability> addAvailability(@PathVariable String username, @RequestBody CoachAvailability a) {
        var u = users.findByUsername(username).orElseThrow();
        a.setCoachId(u.getId()); a.setCoachName(username); a.setStatus("OPEN");
        return ApiResponse.ok(availabilities.save(a));
    }

    @PostMapping("/private-bookings/{username}/{availabilityId}")
    public ApiResponse<PrivateBooking> privateBook(@PathVariable String username, @PathVariable Long availabilityId) {
        var u = users.findByUsername(username).orElseThrow();
        var a = availabilities.findById(availabilityId).orElseThrow();
        var b = new PrivateBooking();
        b.setUsername(username); b.setUserId(u.getId()); b.setAvailabilityId(a.getId());
        return ApiResponse.ok(privateBookings.save(b));
    }

    @GetMapping("/private-bookings/{username}")
    public ApiResponse<List<PrivateBooking>> privateBookings(@PathVariable String username) {
        return ApiResponse.ok(privateBookings.findByUsernameOrderByCreateTimeDesc(username));
    }

    @GetMapping("/private-bookings")
    public ApiResponse<List<PrivateBooking>> privateBookings() {
        return ApiResponse.ok(privateBookings.findAll());
    }

    @PutMapping("/private-bookings/{id}/approve")
    public ApiResponse<PrivateBooking> approvePrivateBooking(@PathVariable Long id) {
        var booking = privateBookings.findById(id).orElseThrow();
        booking.setStatus("APPROVED");
        var slot = availabilities.findById(booking.getAvailabilityId()).orElseThrow();
        slot.setStatus("BOOKED");
        availabilities.save(slot);
        return ApiResponse.ok(privateBookings.save(booking));
    }

    @PutMapping("/private-bookings/{id}/reject")
    public ApiResponse<PrivateBooking> rejectPrivateBooking(@PathVariable Long id, @RequestParam(required = false) String reason) {
        var booking = privateBookings.findById(id).orElseThrow();
        booking.setStatus("REJECTED");
        booking.setRejectReason(reason);
        return ApiResponse.ok(privateBookings.save(booking));
    }

    // ==================== 会员卡 ====================
    @GetMapping("/cards")
    public ApiResponse<List<MembershipCard>> cards(@RequestParam(defaultValue = "false") boolean all) {
        return ApiResponse.ok(service.cards(all));
    }
    @PostMapping("/cards")
    public ApiResponse<MembershipCard> saveCard(@RequestBody MembershipCard card) {
        return ApiResponse.ok(service.saveCard(card));
    }
    @PutMapping("/cards/{id}")
    public ApiResponse<MembershipCard> updateCard(@PathVariable Long id, @RequestBody MembershipCard card) {
        return ApiResponse.ok(service.updateCard(id, card));
    }
    @PostMapping("/cards/purchase")
    public ApiResponse<GymOrder> purchase(@Valid @RequestBody PurchaseRequest request) {
        return ApiResponse.ok(service.purchase(request.username(), request.cardId()));
    }
    @GetMapping("/cards/owned/{username}")
    public ApiResponse<List<GymOrder>> refundableOrders(@PathVariable String username) {
        return ApiResponse.ok(service.refundableOrders(username));
    }
    @GetMapping("/memberships/{username}")
    public ApiResponse<List<MembershipResponse>> memberships(@PathVariable String username) {
        return ApiResponse.ok(service.memberships(username));
    }

    // ==================== 订单 / 退款 ====================
    @GetMapping("/orders/{username}")
    public ApiResponse<List<GymOrder>> orders(@PathVariable String username) {
        return ApiResponse.ok(service.orders(username));
    }
    @PostMapping("/refunds")
    public ApiResponse<RefundApplication> refund(@Valid @RequestBody RefundRequest request) {
        return ApiResponse.ok(service.refund(request.username(), request.orderId(), request.reason(), request.description()));
    }
    @GetMapping("/refunds/pending")
    public ApiResponse<List<RefundApplication>> refundsPending() {
        return ApiResponse.ok(service.refundsPending());
    }
    @GetMapping("/refunds")
    public ApiResponse<List<RefundApplication>> refundsAll() {
        return ApiResponse.ok(service.refundsAll());
    }
    @PutMapping("/refunds/{id}/approve")
    public ApiResponse<RefundApplication> approveRefund(@PathVariable Long id) {
        return ApiResponse.ok(service.approveRefund(id));
    }
    @PutMapping("/refunds/{id}/reject")
    public ApiResponse<RefundApplication> rejectRefund(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return ApiResponse.ok(service.rejectRefund(id, reason));
    }

    // ==================== 运动记录 ====================
    @GetMapping("/exercise/{username}")
    public ApiResponse<List<ExerciseRecord>> exercises(@PathVariable String username,
                                                       @RequestParam(required = false) Integer year,
                                                       @RequestParam(required = false) Integer month) {
        return ApiResponse.ok(service.exercises(username, year, month));
    }

    @GetMapping("/exercise/{username}/monthly")
    public ApiResponse<MonthlyExerciseResponse> exerciseMonthly(@PathVariable String username,
                                                                @RequestParam(required = false) Integer year,
                                                                @RequestParam(required = false) Integer month) {
        YearMonth current = YearMonth.now();
        return ApiResponse.ok(service.exerciseMonthly(username,
                year == null ? current.getYear() : year,
                month == null ? current.getMonthValue() : month));
    }

    @PostMapping("/exercise/{username}")
    public ApiResponse<ExerciseRecord> addExercise(@PathVariable String username, @Valid @RequestBody ExerciseRequest request) {
        return ApiResponse.ok(service.addExercise(username, request));
    }

    @PutMapping("/exercise/{id}")
    public ApiResponse<ExerciseRecord> updateExercise(@PathVariable Long id, @RequestBody ExerciseRequest request) {
        return ApiResponse.ok(service.updateExercise(id, request));
    }

    @DeleteMapping("/exercise/{id}")
    public ApiResponse<Void> deleteExercise(@PathVariable Long id) {
        service.deleteExercise(id);
        return ApiResponse.ok();
    }

    // ==================== 课程 / 预约 / 签到 ====================
    @GetMapping("/courses")
    public ApiResponse<List<GymCourse>> courses() {
        return ApiResponse.ok(service.courses());
    }
    @GetMapping("/courses/{id}")
    public ApiResponse<CourseDetailResponse> courseDetail(@PathVariable Long id) {
        return ApiResponse.ok(service.courseDetail(id));
    }
    @PostMapping("/courses")
    public ApiResponse<GymCourse> saveCourse(@RequestBody GymCourse course) {
        return ApiResponse.ok(service.saveCourse(course));
    }
    @PutMapping("/courses/{id}")
    public ApiResponse<GymCourse> updateCourse(@PathVariable Long id, @RequestBody GymCourse course) {
        return ApiResponse.ok(service.updateCourse(id, course));
    }
    @PostMapping("/courses/{courseId}/book/{username}")
    public ApiResponse<BookingResponse> book(@PathVariable Long courseId, @PathVariable String username) {
        return ApiResponse.ok(service.book(username, courseId));
    }
    @GetMapping("/bookings/{username}")
    public ApiResponse<List<BookingResponse>> bookings(@PathVariable String username) {
        return ApiResponse.ok(service.bookings(username));
    }
    @PutMapping("/bookings/{id}/cancel")
    public ApiResponse<Void> cancelBooking(@PathVariable Long id) {
        service.cancelBooking(id);
        return ApiResponse.ok();
    }

    // ==================== 通知 ====================
    @GetMapping("/notifications/{role}")
    public ApiResponse<List<GymNotification>> notifications(@PathVariable String role,
                                                            @RequestParam(required = false) Long userId) {
        return ApiResponse.ok(service.notifications(role, userId));
    }
    @PostMapping("/notifications")
    public ApiResponse<GymNotification> saveNotification(@RequestBody GymNotification notification) {
        return ApiResponse.ok(service.saveNotification(notification));
    }

    // ==================== 用户管理 ====================
    @GetMapping("/users")
    public ApiResponse<List<User>> users() {
        return ApiResponse.ok(service.users());
    }
    @GetMapping("/users/search")
    public ApiResponse<List<User>> searchUsers(@RequestParam String keyword) {
        return ApiResponse.ok(service.searchUsers(keyword));
    }
    @PutMapping("/users/{id}/status")
    public ApiResponse<User> updateStatus(@PathVariable Long id, @RequestParam String value) {
        return ApiResponse.ok(service.updateStatus(id, value));
    }

    // ==================== 系统配置 ====================
    @GetMapping("/config")
    public ApiResponse<List<SystemConfig>> configs() {
        return ApiResponse.ok(service.configs());
    }
    @PutMapping("/config/{key}")
    public ApiResponse<SystemConfig> updateConfig(@PathVariable String key, @RequestParam String value) {
        return ApiResponse.ok(service.updateConfig(key, value));
    }

    // ==================== 数据看板 ====================
    @GetMapping("/admin/dashboard/stats")
    public ApiResponse<Map<String, Object>> dashboardStats() {
        return ApiResponse.ok(service.dashboardStats());
    }
    @GetMapping("/admin/dashboard/trend")
    public ApiResponse<List<Map<String, Object>>> dashboardTrend() {
        return ApiResponse.ok(service.dashboardTrend());
    }
    @GetMapping("/admin/dashboard/top-courses")
    public ApiResponse<List<TopCourseResponse>> topCourses() {
        return ApiResponse.ok(service.topCourses(3));
    }
}
