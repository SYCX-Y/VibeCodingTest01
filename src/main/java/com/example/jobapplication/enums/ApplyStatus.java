package com.example.jobapplication.enums;

import lombok.Getter;

/**
 * 投递状态
 */
@Getter
public enum ApplyStatus {

    APPLIED(0, "已投递", false),
    PENDING_INTERVIEW(1, "待面试", false),
    INTERVIEWED(2, "已面试", false),
    OFFERED(3, "已录用", true),
    REJECTED(4, "已拒绝", true),
    ABANDONED(5, "已放弃", true);

    private final int code;
    private final String label;
    /** 是否为终态 */
    private final boolean terminal;

    ApplyStatus(int code, String label, boolean terminal) {
        this.code = code;
        this.label = label;
        this.terminal = terminal;
    }

    public static ApplyStatus of(int code) {
        for (ApplyStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("未知投递状态: " + code);
    }
}
