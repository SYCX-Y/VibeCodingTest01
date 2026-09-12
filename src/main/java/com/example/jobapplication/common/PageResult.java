package com.example.jobapplication.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页结果
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private long total;
    private long page;
    private long pageSize;
    private List<T> records;
}
