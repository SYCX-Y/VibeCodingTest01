package com.example.jobapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修改投递记录请求（PUT 全字段覆盖）
 */
@Data
public class ApplicationUpdateRequest {

    @NotBlank(message = "公司名不能为空")
    @Size(max = 100, message = "公司名长度不能超过 100")
    private String companyName;

    @NotNull(message = "投递时间不能为空")
    private LocalDateTime applyTime;

    @NotBlank(message = "投递方式不能为空")
    @Size(max = 50, message = "投递方式长度不能超过 50")
    private String applyChannel;

    @NotBlank(message = "投递岗位不能为空")
    @Size(max = 100, message = "投递岗位长度不能超过 100")
    private String positionName;

    private Integer interviewType;

    private Integer interviewScore;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;
}
