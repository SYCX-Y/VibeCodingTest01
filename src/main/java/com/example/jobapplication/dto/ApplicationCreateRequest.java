package com.example.jobapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新增投递记录请求
 */
@Data
public class ApplicationCreateRequest {

    @NotBlank(message = "公司名不能为空")
    @Size(max = 100, message = "公司名长度不能超过 100")
    private String companyName;

    /** 投递时间，不填默认当前时间 */
    private LocalDateTime applyTime;

    @NotBlank(message = "投递方式不能为空")
    @Size(max = 50, message = "投递方式长度不能超过 50")
    private String applyChannel;

    @NotBlank(message = "投递岗位不能为空")
    @Size(max = 100, message = "投递岗位长度不能超过 100")
    private String positionName;

    /** 面试形式：0=未面试（默认），1=线上面试，2=线下面试 */
    private Integer interviewType;

    /** 面试评分（1-10），未面试时为空 */
    private Integer interviewScore;

    /** 投递状态：默认 0=已投递 */
    private Integer status;

    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;
}
