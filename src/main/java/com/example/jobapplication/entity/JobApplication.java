package com.example.jobapplication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历投递记录实体，对应表 job_application
 */
@Data
@TableName("job_application")
public class JobApplication {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公司名 */
    private String companyName;

    /** 投递时间 */
    private LocalDateTime applyTime;

    /** 投递方式（使用的软件） */
    private String applyChannel;

    /** 投递岗位 */
    private String positionName;

    /** 面试形式：0=未面试，1=线上面试，2=线下面试 */
    private Integer interviewType;

    /** 面试评分（1-10 的整数），未面试为空 */
    private Integer interviewScore;

    /** 投递状态：0=已投递，1=待面试，2=已面试，3=已录用，4=已拒绝，5=已放弃 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
