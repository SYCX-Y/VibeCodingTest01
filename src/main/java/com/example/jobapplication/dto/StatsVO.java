package com.example.jobapplication.dto;

import lombok.Data;

import java.util.List;

/**
 * 统计看板数据
 */
@Data
public class StatsVO {

    /** 近 12 周投递量趋势 */
    private List<WeeklyTrend> weeklyTrend;

    /** 投递方式分布 */
    private List<NameValue> channelDist;

    /** 状态分布 */
    private List<NameValue> statusDist;

    /** 转化漏斗 */
    private Funnel funnel;

    @Data
    public static class WeeklyTrend {
        /** 周标签，如 7/14 */
        private String week;
        private long count;
    }

    @Data
    public static class NameValue {
        private String name;
        private long value;
    }

    @Data
    public static class Funnel {
        /** 投递（全部记录） */
        private long applied;
        /** 待面试（当前状态为 待面试 / 已面试 / 已录用） */
        private long pending;
        /** 已面试（当前状态为 已面试 / 已录用） */
        private long interviewed;
        /** 已录用 */
        private long offered;
    }
}
