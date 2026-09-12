package com.example.jobapplication.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递记录响应对象
 */
@Data
public class ApplicationVO {

    private Long id;
    private String companyName;
    private LocalDateTime applyTime;
    private String applyChannel;
    private String positionName;
    private Integer interviewType;
    /** 面试形式中文名，如 线上面试 */
    private String interviewTypeLabel;
    private Integer interviewScore;
    private Integer status;
    /** 状态中文名，如 待面试 */
    private String statusLabel;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
