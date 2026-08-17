package com.example.gym_server.service;

import com.example.gym_server.constant.ErrorCode;
import com.example.gym_server.entity.CoachApply;
import com.example.gym_server.entity.CoachInfo;
import com.example.gym_server.entity.GymNotification;
import com.example.gym_server.entity.User;
import com.example.gym_server.exception.BusinessException;
import com.example.gym_server.repository.CoachApplyRepository;
import com.example.gym_server.repository.GymNotificationRepository;
import com.example.gym_server.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CoachApplyService {

    private final CoachApplyRepository coachApplyRepository;
    private final UserRepository userRepository;
    private final CoachInfoService coachInfoService;
    private final GymNotificationRepository notificationRepository;

    public CoachApplyService(CoachApplyRepository coachApplyRepository, UserRepository userRepository,
                             CoachInfoService coachInfoService, GymNotificationRepository notificationRepository) {
        this.coachApplyRepository = coachApplyRepository;
        this.userRepository = userRepository;
        this.coachInfoService = coachInfoService;
        this.notificationRepository = notificationRepository;
    }

    // 用户提交申请
    public CoachApply apply(CoachApply coachApply) {
        if (!userRepository.existsById(coachApply.getUserId())) {
            throw BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        if (coachApplyRepository.findTopByUserIdOrderByCreateTimeDesc(coachApply.getUserId())
                .filter(a -> "PENDING".equals(a.getStatus())).isPresent()) {
            throw BusinessException.of(ErrorCode.APPLY_ALREADY_SUBMITTED, "您已提交申请，请等待审核");
        }
        coachApply.setStatus("PENDING");
        coachApply.setCreateTime(LocalDateTime.now());
        return coachApplyRepository.save(coachApply);
    }

    // 管理员查看所有申请
    public List<CoachApply> getAllApply() {
        return coachApplyRepository.findAll();
    }

    public CoachApply getLatest(Long userId) { return coachApplyRepository.findTopByUserIdOrderByCreateTimeDesc(userId).orElse(null); }

    // 管理员通过申请
    public void pass(Long id) {
        CoachApply apply = coachApplyRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "申请不存在"));

        apply.setStatus("PASSED");
        coachApplyRepository.save(apply);

        User user = userRepository.findById(apply.getUserId())
                .orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在"));
        user.setRole("COACH");
        userRepository.save(user);

        // 创建教练资料
        CoachInfo coachInfo = new CoachInfo();
        coachInfo.setUserId(apply.getUserId());
        coachInfo.setName(apply.getName());
        coachInfo.setIntroduction(apply.getDescription());
        coachInfo.setCreateTime(LocalDateTime.now());
        coachInfoService.create(coachInfo);

        notifyUser(apply.getUserId(), "教练入驻审核通过", "恭喜，您的教练入驻申请已通过审核，现在可以发布课程与空闲时间了。");
    }

    // 管理员拒绝申请
    public void reject(Long id, String rejectReason) {
        CoachApply apply = coachApplyRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "申请不存在"));
        apply.setStatus("REJECTED");
        apply.setRejectReason(rejectReason);
        coachApplyRepository.save(apply);

        notifyUser(apply.getUserId(), "教练入驻审核未通过",
                rejectReason == null || rejectReason.isBlank() ? "很遗憾，您的教练入驻申请未通过审核。" : "很遗憾，您的教练入驻申请未通过审核：" + rejectReason);
    }

    private void notifyUser(Long userId, String title, String content) {
        GymNotification notification = new GymNotification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetRole("USER");
        notification.setTargetUserIds(String.valueOf(userId));
        notification.setCreateTime(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
