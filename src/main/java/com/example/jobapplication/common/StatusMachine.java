package com.example.jobapplication.common;

import com.example.jobapplication.enums.ApplyStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 投递状态机：定义合法状态流转路径
 *
 * 0 已投递 -> 1 待面试 / 4 已拒绝 / 5 已放弃
 * 1 待面试 -> 2 已面试 / 5 已放弃
 * 2 已面试 -> 3 已录用 / 4 已拒绝 / 5 已放弃
 * 3 已录用 / 4 已拒绝 / 5 已放弃：终态，不可再流转
 */
public final class StatusMachine {

    private static final Map<Integer, Set<Integer>> TRANSITIONS = new HashMap<>();

    static {
        TRANSITIONS.put(ApplyStatus.APPLIED.getCode(), Set.of(
                ApplyStatus.PENDING_INTERVIEW.getCode(),
                ApplyStatus.REJECTED.getCode(),
                ApplyStatus.ABANDONED.getCode()));
        TRANSITIONS.put(ApplyStatus.PENDING_INTERVIEW.getCode(), Set.of(
                ApplyStatus.INTERVIEWED.getCode(),
                ApplyStatus.ABANDONED.getCode()));
        TRANSITIONS.put(ApplyStatus.INTERVIEWED.getCode(), Set.of(
                ApplyStatus.OFFERED.getCode(),
                ApplyStatus.REJECTED.getCode(),
                ApplyStatus.ABANDONED.getCode()));
        TRANSITIONS.put(ApplyStatus.OFFERED.getCode(), Set.of());
        TRANSITIONS.put(ApplyStatus.REJECTED.getCode(), Set.of());
        TRANSITIONS.put(ApplyStatus.ABANDONED.getCode(), Set.of());
    }

    private StatusMachine() {
    }

    /** 判断 from -> to 是否为合法流转 */
    public static boolean canTransition(int from, int to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    /** 判断是否为终态（已录用 / 已拒绝 / 已放弃） */
    public static boolean isTerminal(int status) {
        return status == ApplyStatus.OFFERED.getCode()
                || status == ApplyStatus.REJECTED.getCode()
                || status == ApplyStatus.ABANDONED.getCode();
    }
}
