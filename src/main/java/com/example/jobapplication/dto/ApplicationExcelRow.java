package com.example.jobapplication.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * Excel 导出行（列名与 PRD FR-8 一致）
 *
 * 列宽说明：Excel 列宽单位 ≈ 1 个半角字符；中文字符按 2 计。
 * 各列宽度按字段最长内容估算并留有余量，保证导出后信息完整显示。
 */
@Data
public class ApplicationExcelRow {

    @ExcelProperty("id")
    @ColumnWidth(8)
    private Long id;

    @ExcelProperty("公司名")
    @ColumnWidth(22)
    private String companyName;

    @ExcelProperty("投递时间")
    @ColumnWidth(22)
    private String applyTime;

    @ExcelProperty("投递方式")
    @ColumnWidth(14)
    private String applyChannel;

    @ExcelProperty("投递岗位")
    @ColumnWidth(24)
    private String positionName;

    @ExcelProperty("面试形式")
    @ColumnWidth(12)
    private String interviewTypeLabel;

    @ExcelProperty("面试评分")
    @ColumnWidth(10)
    private Integer interviewScore;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String statusLabel;

    @ExcelProperty("备注")
    @ColumnWidth(50)
    private String remark;
}
