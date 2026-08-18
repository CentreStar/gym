package com.example.gym_server.service;

import com.example.gym_server.constant.ErrorCode;
import com.example.gym_server.dto.*;
import com.example.gym_server.entity.*;
import com.example.gym_server.exception.BusinessException;
import com.example.gym_server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GymBusinessService {
    private final MembershipCardRepository cards;
    private final GymOrderRepository orders;
    private final GymCourseRepository courses;
    private final CourseBookingRepository bookings;
    private final GymNotificationRepository notifications;
    private final UserRepository users;
    private final ExerciseRecordRepository exerciseRecords;
    private final RefundApplicationRepository refunds;
    private final UserMembershipRepository memberships;
    private final GymVisitRepository visits;
    private final SystemConfigRepository configs;

    public GymBusinessService(MembershipCardRepository cards, GymOrderRepository orders, GymCourseRepository courses,
                              CourseBookingRepository bookings, GymNotificationRepository notifications, UserRepository users,
                              ExerciseRecordRepository exerciseRecords, RefundApplicationRepository refunds,
                              UserMembershipRepository memberships, GymVisitRepository visits, SystemConfigRepository configs) {
        this.cards = cards; this.orders = orders; this.courses = courses; this.bookings = bookings;
        this.notifications = notifications; this.users = users; this.exerciseRecords = exerciseRecords;
        this.refunds = refunds; this.memberships = memberships; this.visits = visits; this.configs = configs;
    }

    // ==================== 会员卡 ====================
    public List<MembershipCard> cards(boolean all) { return all ? cards.findAll() : cards.findByEnabledTrueOrderByPriceAsc(); }
    public MembershipCard saveCard(MembershipCard card) { card.setCreateTime(LocalDateTime.now()); return cards.save(card); }
    public MembershipCard updateCard(Long id, MembershipCard update) {
        MembershipCard card = cards.findById(id).orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "会员卡不存在"));
        if (update.getName() != null) card.setName(update.getName());
        if (update.getValidDays() != null) card.setValidDays(update.getValidDays());
        if (update.getPrice() != null) card.setPrice(update.getPrice());
        if (update.getDescription() != null) card.setDescription(update.getDescription());
        card.setEnabled(update.isEnabled());
        return cards.save(card);
    }

    // ==================== 课程 ====================
    public List<GymCourse> courses() { return courses.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now().minusDays(1)); }
    public GymCourse saveCourse(GymCourse course) { return courses.save(course); }
    public GymCourse updateCourse(Long id, GymCourse update) {
        GymCourse course = courses.findById(id).orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "课程不存在"));
        if (update.getTitle() != null) course.setTitle(update.getTitle());
        if (update.getType() != null) course.setType(update.getType());
        if (update.getCoachName() != null) course.setCoachName(update.getCoachName());
        if (update.getStartTime() != null) course.setStartTime(update.getStartTime());
        if (update.getCapacity() != null) course.setCapacity(update.getCapacity());
        if (update.getPrice() != null) course.setPrice(update.getPrice());
        if (update.getDescription() != null) course.setDescription(update.getDescription());
        return courses.save(course);
    }
    public CourseDetailResponse courseDetail(Long id) {
        GymCourse course = courses.findById(id).orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "课程不存在"));
        long booked = bookings.countByCourseIdAndStatus(id, "BOOKED");
        long capacity = course.getCapacity() == null ? 0 : course.getCapacity();
        return new CourseDetailResponse(course, booked, Math.max(0, capacity - booked));
    }

    // ==================== 通知 ====================
    public List<GymNotification> notifications(String role, Long userId) {
        List<GymNotification> list = notifications.findByTargetRoleInOrderByCreateTimeDesc(List.of("ALL", role));
        return list.stream().filter(n -> visibleTo(n, userId)).toList();
    }
    private boolean visibleTo(GymNotification n, Long userId) {
        if (n.getTargetUserIds() == null || n.getTargetUserIds().isBlank()) return true;
        if (userId == null) return false;
        for (String part : n.getTargetUserIds().split(",")) {
            if (part.trim().equals(String.valueOf(userId))) return true;
        }
        return false;
    }
    public GymNotification saveNotification(GymNotification notification) {
        notification.setCreateTime(LocalDateTime.now());
        if (notification.getTargetRole() == null || notification.getTargetRole().isBlank()) notification.setTargetRole("ALL");
        return notifications.save(notification);
    }

    // ==================== 用户 ====================
    public List<User> users() { return users.findAll(); }
    public List<User> searchUsers(String keyword) {
        return users.findAll().stream()
                .filter(u -> u.getUsername() != null && u.getUsername().contains(keyword))
                .toList();
    }
    public User updateStatus(Long id, String status) {
        User user = users.findById(id).orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在"));
        user.setStatus(status);
        return users.save(user);
    }

    // ==================== 购买 / 权益 ====================
    @Transactional
    public GymOrder purchase(String username, Long cardId) {
        User user = findUser(username);
        MembershipCard card = cards.findById(cardId).orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "会员卡不存在"));
        if (!card.isEnabled()) throw BusinessException.of(ErrorCode.BUSINESS_CONFLICT, "该会员卡已下架");
        GymOrder order = new GymOrder();
        order.setUserId(user.getId()); order.setCardId(cardId); order.setType("MEMBERSHIP");
        order.setTitle(card.getName()); order.setAmount(card.getPrice());
        order = orders.save(order);

        // 购买成功 → 生成会员权益记录
        UserMembership membership = new UserMembership();
        membership.setUserId(user.getId());
        membership.setCardId(cardId);
        membership.setOrderId(order.getId());
        membership.setStartDate(LocalDateTime.now());
        membership.setEndDate(LocalDateTime.now().plusDays(card.getValidDays() == null ? 0 : card.getValidDays()));
        membership.setStatus("ACTIVE");
        memberships.save(membership);
        return order;
    }

    public List<GymOrder> orders(String username) { return orders.findByUserIdOrderByCreateTimeDesc(findUser(username).getId()); }
    /** 可退的会员卡订单（用于退卡申请）。 */
    public List<GymOrder> refundableOrders(String username) {
        return orders(username).stream()
                .filter(o -> "MEMBERSHIP".equals(o.getType()) && "PAID".equals(o.getStatus()))
                .toList();
    }
    public List<MembershipResponse> memberships(String username) {
        Long userId = findUser(username).getId();
        return memberships.findByUserIdOrderByStartDateDesc(userId).stream()
                .map(m -> new MembershipResponse(m.getId(), m.getCardId(),
                        cards.findById(m.getCardId()).map(MembershipCard::getName).orElse("会员卡"),
                        m.getStartDate(), m.getEndDate(), m.getStatus()))
                .toList();
    }

    // ==================== 退款 ====================
    @Transactional
    public RefundApplication refund(String username, Long orderId, String reason, String description) {
        User user = findUser(username);
        GymOrder order = orders.findById(orderId).orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在"));
        if (!user.getId().equals(order.getUserId()) || !"MEMBERSHIP".equals(order.getType()) || !"PAID".equals(order.getStatus())) {
            throw BusinessException.of(ErrorCode.BUSINESS_CONFLICT, "该会员卡不可退");
        }
        RefundApplication application = new RefundApplication();
        application.setUserId(user.getId()); application.setOrderId(orderId);
        application.setCardName(order.getTitle()); application.setRefundAmount(order.getAmount());
        application.setReason(reason); application.setDescription(description);
        order.setStatus("REFUND_PENDING");
        orders.save(order);
        return refunds.save(application);
    }
    public List<RefundApplication> refundsPending() { return refunds.findByStatusOrderByCreateTimeDesc("PENDING"); }
    public List<RefundApplication> refundsAll() { return refunds.findAllByOrderByCreateTimeDesc(); }

    @Transactional
    public RefundApplication approveRefund(Long id) {
        RefundApplication app = refunds.findById(id).orElseThrow(() -> BusinessException.of(ErrorCode.REFUND_NOT_FOUND, "退款申请不存在"));
        if (!"PENDING".equals(app.getStatus())) throw BusinessException.of(ErrorCode.BUSINESS_CONFLICT, "该申请已处理");
        app.setStatus("APPROVED");
        refunds.save(app);
        orders.findById(app.getOrderId()).ifPresent(order -> { order.setStatus("REFUNDED"); orders.save(order); });
        memberships.findByOrderId(app.getOrderId()).ifPresent(m -> { m.setStatus("CANCELLED"); memberships.save(m); });
        return app;
    }

    @Transactional
    public RefundApplication rejectRefund(Long id, String rejectReason) {
        RefundApplication app = refunds.findById(id).orElseThrow(() -> BusinessException.of(ErrorCode.REFUND_NOT_FOUND, "退款申请不存在"));
        if (!"PENDING".equals(app.getStatus())) throw BusinessException.of(ErrorCode.BUSINESS_CONFLICT, "该申请已处理");
        app.setStatus("REJECTED");
        app.setRejectReason(rejectReason);
        refunds.save(app);
        orders.findById(app.getOrderId()).ifPresent(order -> {
            if ("REFUND_PENDING".equals(order.getStatus())) { order.setStatus("PAID"); orders.save(order); }
        });
        return app;
    }

    // ==================== 运动记录 ====================
    public ExerciseRecord addExercise(String username, ExerciseRequest request) {
        ExerciseRecord record = fromRequest(request);
        record.setUserId(findUser(username).getId());
        if (record.getExerciseDate() == null) record.setExerciseDate(LocalDate.now());
        return exerciseRecords.save(record);
    }

    public List<ExerciseRecord> exercises(String username, Integer year, Integer month) {
        Long userId = findUser(username).getId();
        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            return exerciseRecords.findByUserIdAndExerciseDateBetweenOrderByExerciseDateDesc(
                    userId, ym.atDay(1), ym.plusMonths(1).atDay(1));
        }
        return exerciseRecords.findByUserIdOrderByExerciseDateDesc(userId);
    }

    /** 月度运动统计：只依据 exercise_records.exercise_date。 */
    public MonthlyExerciseResponse exerciseMonthly(String username, int year, int month) {
        Long userId = findUser(username).getId();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);
        long days = exerciseRecords.countDistinctExerciseDays(userId, start, end);
        long minutes = exerciseRecords.sumDurationMinutes(userId, start, end);
        List<String> dates = exerciseRecords.distinctExerciseDates(userId, start, end).stream()
                .map(LocalDate::toString).toList();
        return new MonthlyExerciseResponse(year, month, days, minutes, dates);
    }

    public ExerciseRecord updateExercise(Long id, ExerciseRequest request) {
        ExerciseRecord record = exerciseRecords.findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "运动记录不存在"));
        ExerciseRecord updated = fromRequest(request);
        record.setExerciseType(updated.getExerciseType());
        record.setBodyParts(updated.getBodyParts());
        record.setActionName(updated.getActionName());
        record.setWeight(updated.getWeight());
        record.setSets(updated.getSets());
        record.setHandleType(updated.getHandleType());
        record.setResistance(updated.getResistance());
        record.setSpeed(updated.getSpeed());
        record.setIncline(updated.getIncline());
        record.setDurationMinutes(updated.getDurationMinutes());
        record.setExerciseDate(updated.getExerciseDate() == null ? LocalDate.now() : updated.getExerciseDate());
        return exerciseRecords.save(record);
    }

    public void deleteExercise(Long id) {
        exerciseRecords.deleteById(id);
    }

    private ExerciseRecord fromRequest(ExerciseRequest request) {
        ExerciseRecord record = new ExerciseRecord();
        record.setExerciseType(request.getExerciseType());
        record.setBodyParts(request.getBodyParts() == null || request.getBodyParts().isEmpty()
                ? null : String.join(",", request.getBodyParts()));
        record.setActionName(request.getActionName());
        record.setWeight(request.getWeight());
        record.setSets(request.getSets());
        record.setHandleType(request.getHandleType());
        record.setResistance(request.getResistance());
        record.setSpeed(request.getSpeed());
        record.setIncline(request.getIncline());
        record.setDurationMinutes(request.getDurationMinutes());
        record.setExerciseDate(request.getExerciseDate());
        return record;
    }

    // ==================== 课程预约 ====================
    @Transactional
    public BookingResponse book(String username, Long courseId) {
        User user = findUser(username);
        GymCourse course = courses.findById(courseId).orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "课程不存在"));
        if (bookings.findByUserIdAndCourseId(user.getId(), courseId).isPresent()) {
            throw BusinessException.of(ErrorCode.ALREADY_BOOKED, "您已预约该课程");
        }
        long booked = bookings.countByCourseIdAndStatus(courseId, "BOOKED");
        if (course.getCapacity() != null && booked >= course.getCapacity()) {
            throw BusinessException.of(ErrorCode.COURSE_FULL, "课程已满员");
        }
        CourseBooking booking = new CourseBooking();
        booking.setUserId(user.getId()); booking.setCourseId(courseId);
        booking = bookings.save(booking);
        return new BookingResponse(booking.getId(), booking.getStatus(), course);
    }
    public List<BookingResponse> bookings(String username) {
        return bookings.findByUserIdAndStatus(findUser(username).getId(), "BOOKED").stream()
                .map(b -> new BookingResponse(b.getId(), b.getStatus(), courses.findById(b.getCourseId()).orElse(null)))
                .toList();
    }
    public void cancelBooking(Long bookingId) {
        CourseBooking booking = bookings.findById(bookingId).orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "预约不存在"));
        booking.setStatus("CANCELLED");
        bookings.save(booking);
    }

    // ==================== 系统配置 ====================
    public List<SystemConfig> configs() { return configs.findAll(); }
    public SystemConfig updateConfig(String key, String value) {
        SystemConfig config = configs.findByConfigKey(key)
                .orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "配置项不存在"));
        config.setConfigValue(value);
        return configs.save(config);
    }

    // ==================== 数据看板 ====================
    public Map<String, Object> dashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime monthStart = YearMonth.from(now).atDay(1).atStartOfDay();
        LocalDateTime monthEnd = YearMonth.from(now).plusMonths(1).atDay(1).atStartOfDay();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", users.count());
        stats.put("todayVisits", visits.countByEnteredAtBetween(todayStart, todayStart.plusDays(1)));
        stats.put("monthNewUsers", users.countByCreateTimeBetween(monthStart, monthEnd));
        BigDecimal revenue = orders.sumRevenueBetween(monthStart, monthEnd);
        stats.put("monthRevenue", revenue == null ? BigDecimal.ZERO : revenue);
        return stats;
    }

    public List<Map<String, Object>> dashboardTrend() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            long count = visits.countByEnteredAtBetween(start, start.plusDays(1));
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", day.toString());
            point.put("count", count);
            result.add(point);
        }
        return result;
    }

    public List<TopCourseResponse> topCourses(int limit) {
        return bookings.countBookingsGroupByCourse().stream()
                .limit(limit)
                .map(row -> {
                    Long courseId = (Long) row[0];
                    long count = (Long) row[1];
                    GymCourse course = courses.findById(courseId).orElse(null);
                    return new TopCourseResponse(courseId,
                            course == null ? "未知课程" : course.getTitle(),
                            course == null ? "" : course.getType(),
                            count);
                })
                .toList();
    }

    private User findUser(String username) {
        return users.findByUsername(username).orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在"));
    }
}
