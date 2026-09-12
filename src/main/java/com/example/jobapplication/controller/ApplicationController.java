package com.example.jobapplication.controller;

import com.example.jobapplication.common.PageResult;
import com.example.jobapplication.common.Result;
import com.example.jobapplication.dto.ApplicationCreateRequest;
import com.example.jobapplication.dto.ApplicationQuery;
import com.example.jobapplication.dto.ApplicationUpdateRequest;
import com.example.jobapplication.dto.ApplicationVO;
import com.example.jobapplication.dto.StatsVO;
import com.example.jobapplication.service.ApplicationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 投递记录接口
 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /** API-1 新增投递记录 */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid ApplicationCreateRequest request) {
        return Result.ok(applicationService.create(request));
    }

    /** API-2 分页查询（支持筛选与搜索） */
    @GetMapping
    public Result<PageResult<ApplicationVO>> page(ApplicationQuery query) {
        return Result.ok(applicationService.page(query));
    }

    /** API-3 查询记录详情 */
    @GetMapping("/{id}")
    public Result<ApplicationVO> detail(@PathVariable Long id) {
        return Result.ok(applicationService.detail(id));
    }

    /** API-4 修改记录（含状态流转校验） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid ApplicationUpdateRequest request) {
        applicationService.update(id, request);
        return Result.ok();
    }

    /** API-5 删除记录 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return Result.ok();
    }

    /** API-6 按当前筛选条件导出 Excel */
    @GetMapping("/export")
    public void export(ApplicationQuery query, HttpServletResponse response) {
        applicationService.export(query, response);
    }

    /** API-7 统计看板数据 */
    @GetMapping("/stats")
    public Result<StatsVO> stats() {
        return Result.ok(applicationService.stats());
    }
}
