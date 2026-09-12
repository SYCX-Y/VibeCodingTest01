package com.example.jobapplication.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 列表查询 / 导出筛选条件
 */
@Data
public class ApplicationQuery {

    /** 公司名（模糊） */
    private String companyName;

    /** 岗位名（模糊） */
    private String positionName;

    /** 投递方式（精确） */
    private String applyChannel;

    /** 面试形式（精确） */
    private Integer interviewType;

    /** 状态（精确） */
    private Integer status;

    /** 投递时间范围起 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 投递时间范围止 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer page = 1;

    private Integer pageSize = 10;
}
