package com.example.jobapplication.enums;

import lombok.Getter;

/**
 * 面试形式
 */
@Getter
public enum InterviewType {

    NONE(0, "未面试"),
    ONLINE(1, "线上面试"),
    OFFLINE(2, "线下面试");

    private final int code;
    private final String label;

    InterviewType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InterviewType of(int code) {
        for (InterviewType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        throw new IllegalArgumentException("未知面试形式: " + code);
    }
}
