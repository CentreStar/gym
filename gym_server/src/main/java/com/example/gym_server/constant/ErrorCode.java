package com.example.gym_server.constant;

/**
 * 业务错误码定义（与需求文档 9.2 对齐）。
 */
public final class ErrorCode {

    public static final int SUCCESS = 0;

    // 通用
    public static final int PARAM_INVALID = 1001;
    public static final int RESOURCE_NOT_FOUND = 1002;
    public static final int BUSINESS_CONFLICT = 1003;

    // 用户
    public static final int USERNAME_EXISTS = 2001;
    public static final int PASSWORD_WRONG = 2002;
    public static final int TOKEN_INVALID = 2003;
    public static final int ACCESS_DENIED = 2004;

    // 课程 / 会员卡 / 签到
    public static final int COURSE_FULL = 3001;
    public static final int ALREADY_BOOKED = 3002;
    public static final int NO_ACTIVE_CARD = 3003;
    public static final int ALREADY_INSIDE = 3004;

    // 教练 / 退款
    public static final int APPLY_ALREADY_SUBMITTED = 4001;
    public static final int REFUND_NOT_FOUND = 4002;

    // 系统
    public static final int SYSTEM_BUSY = 5001;
    public static final int DB_ERROR = 5002;

    private ErrorCode() {
    }
}
