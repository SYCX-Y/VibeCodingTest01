package com.example.jobapplication.service;

import com.example.jobapplication.common.PageResult;
import com.example.jobapplication.dto.ApplicationCreateRequest;
import com.example.jobapplication.dto.ApplicationQuery;
import com.example.jobapplication.dto.ApplicationUpdateRequest;
import com.example.jobapplication.dto.ApplicationVO;
import com.example.jobapplication.dto.StatsVO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 投递记录业务接口
 */
public interface ApplicationService {

    /** 新增投递记录，返回新记录 id */
    Long create(ApplicationCreateRequest request);

    /** 分页查询（支持筛选与搜索） */
    PageResult<ApplicationVO> page(ApplicationQuery query);

    /** 查询单条详情 */
    ApplicationVO detail(Long id);

    /** 修改记录（含状态机校验） */
    void update(Long id, ApplicationUpdateRequest request);

    /** 删除记录 */
    void delete(Long id);

    /** 按当前筛选条件导出 Excel */
    void export(ApplicationQuery query, HttpServletResponse response);

    /** 统计看板数据 */
    StatsVO stats();
}
