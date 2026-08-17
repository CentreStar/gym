package com.example.gym_server.service;

import com.example.gym_server.constant.ErrorCode;
import com.example.gym_server.dto.MonthlyAttendanceResponse;
import com.example.gym_server.dto.VisitResponse;
import com.example.gym_server.entity.GymVisit;
import com.example.gym_server.entity.User;
import com.example.gym_server.exception.BusinessException;
import com.example.gym_server.repository.GymVisitRepository;
import com.example.gym_server.repository.UserMembershipRepository;
import com.example.gym_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.HexFormat;
import java.util.List;

@Service
public class GymVisitService {
    private static final String QR_SECRET = "gym_qr_sign_secret_2026";
    private static final long QR_EXPIRE_SECONDS = 300;

    private final GymVisitRepository visits;
    private final UserRepository users;
    private final UserMembershipRepository memberships;

    public GymVisitService(GymVisitRepository visits, UserRepository users, UserMembershipRepository memberships) {
        this.visits = visits; this.users = users; this.memberships = memberships;
    }

    /** 同一二维码切换在场状态：首次扫码入场，再次扫码出场。 */
    @Transactional
    public VisitResponse scan(String qrCode) {
        Long userId = parseQrCode(qrCode);
        User user = users.findById(userId).orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在"));
        if (!"NORMAL".equals(user.getStatus())) throw BusinessException.of(ErrorCode.BUSINESS_CONFLICT, "账户不可用，无法进场");
        LocalDateTime now = LocalDateTime.now();
        return visits.findFirstByUserIdAndExitedAtIsNullOrderByEnteredAtDesc(userId)
                .map(visit -> close(visit, now))
                .orElseGet(() -> {
                    // 入场校验会员卡是否有效
                    if (!memberships.existsByUserIdAndStatusAndEndDateAfter(userId, "ACTIVE", now)) {
                        throw BusinessException.of(ErrorCode.NO_ACTIVE_CARD, "无有效会员卡，请先购买会员卡");
                    }
                    return open(userId, now);
                });
    }

    public MonthlyAttendanceResponse monthly(Long userId, int year, int month) {
        YearMonth selected = YearMonth.of(year, month);
        LocalDateTime start = selected.atDay(1).atStartOfDay();
        LocalDateTime end = selected.plusMonths(1).atDay(1).atStartOfDay();
        List<GymVisit> list = visits.findByUserIdAndEnteredAtGreaterThanEqualAndEnteredAtLessThan(userId, start, end);
        List<Integer> days = list.stream().map(visit -> visit.getEnteredAt().getDayOfMonth()).distinct().sorted().toList();
        long duration = list.stream().mapToLong(visit -> visit.getDurationSeconds() == null ? 0 : visit.getDurationSeconds()).sum();
        return new MonthlyAttendanceResponse(year, month, days.size(), duration, days);
    }

    public long todayCount() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        return visits.countByEnteredAtBetween(start, start.plusDays(1));
    }

    /** 供其它业务（如课程签到）复用：解析二维码并校验签名/有效期，返回 userId。 */
    public Long resolveUserId(String qrCode) {
        return parseQrCode(qrCode);
    }

    private VisitResponse open(Long userId, LocalDateTime now) {
        GymVisit visit = new GymVisit(); visit.setUserId(userId); visit.setEnteredAt(now);
        visit = visits.save(visit);
        return new VisitResponse("ENTRY", visit.getId(), now, null);
    }
    private VisitResponse close(GymVisit visit, LocalDateTime now) {
        visit.setExitedAt(now);
        visit.setDurationSeconds(Math.max(0, Duration.between(visit.getEnteredAt(), now).getSeconds()));
        visits.save(visit);
        return new VisitResponse("EXIT", visit.getId(), now, visit.getDurationSeconds());
    }

    /** 解析并校验二维码：GYM:{userId}:{timestamp}:{sign}，有效期 5 分钟。 */
    private Long parseQrCode(String qrCode) {
        if (qrCode == null || !qrCode.startsWith("GYM:")) {
            throw BusinessException.of(ErrorCode.BUSINESS_CONFLICT, "无效的健身房二维码");
        }
        String[] parts = qrCode.split(":");
        if (parts.length != 4) throw BusinessException.of(ErrorCode.BUSINESS_CONFLICT, "无效的健身房二维码");
        long userId;
        long ts;
        try {
            userId = Long.parseLong(parts[1]);
            ts = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            throw BusinessException.of(ErrorCode.BUSINESS_CONFLICT, "无效的健身房二维码");
        }
        long now = System.currentTimeMillis() / 1000;
        if (Math.abs(now - ts) > QR_EXPIRE_SECONDS) {
            throw BusinessException.of(ErrorCode.BUSINESS_CONFLICT, "二维码已过期，请刷新后重试");
        }
        if (!sign(userId, ts).equals(parts[3])) {
            throw BusinessException.of(ErrorCode.BUSINESS_CONFLICT, "二维码签名无效");
        }
        return userId;
    }

    private String sign(long userId, long ts) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(QR_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal((userId + ":" + ts).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("二维码签名失败", e);
        }
    }
}
