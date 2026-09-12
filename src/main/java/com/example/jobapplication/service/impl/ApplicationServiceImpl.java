package com.example.jobapplication.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jobapplication.common.AutoColumnWidthHandler;
import com.example.jobapplication.common.BusinessException;
import com.example.jobapplication.common.PageResult;
import com.example.jobapplication.common.StatusMachine;
import com.example.jobapplication.dto.ApplicationCreateRequest;
import com.example.jobapplication.dto.ApplicationExcelRow;
import com.example.jobapplication.dto.ApplicationQuery;
import com.example.jobapplication.dto.ApplicationUpdateRequest;
import com.example.jobapplication.dto.ApplicationVO;
import com.example.jobapplication.dto.StatsVO;
import com.example.jobapplication.entity.JobApplication;
import com.example.jobapplication.enums.ApplyStatus;
import com.example.jobapplication.enums.InterviewType;
import com.example.jobapplication.mapper.JobApplicationMapper;
import com.example.jobapplication.service.ApplicationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 投递记录业务实现
 */
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("M/d");
    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JobApplicationMapper applicationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ApplicationCreateRequest request) {
        JobApplication entity = new JobApplication();
        entity.setCompanyName(request.getCompanyName().trim());
        entity.setApplyTime(request.getApplyTime() == null ? LocalDateTime.now() : request.getApplyTime());
        entity.setApplyChannel(request.getApplyChannel().trim());
        entity.setPositionName(request.getPositionName().trim());
        entity.setInterviewType(request.getInterviewType() == null ? InterviewType.NONE.getCode() : request.getInterviewType());
        entity.setInterviewScore(request.getInterviewScore());
        entity.setStatus(request.getStatus() == null ? ApplyStatus.APPLIED.getCode() : request.getStatus());
        entity.setRemark(request.getRemark());
        validateBusinessRules(entity);
        applicationMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public PageResult<ApplicationVO> page(ApplicationQuery query) {
        int pageNum = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 10 : Math.min(Math.max(query.getPageSize(), 1), 100);
        Page<JobApplication> p = new Page<>(pageNum, pageSize);
        applicationMapper.selectPage(p, buildWrapper(query));
        return new PageResult<>(p.getTotal(), p.getCurrent(), p.getSize(), toVOList(p.getRecords()));
    }

    @Override
    public ApplicationVO detail(Long id) {
        return toVO(getByIdOrThrow(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ApplicationUpdateRequest request) {
        JobApplication old = getByIdOrThrow(id);

        JobApplication entity = new JobApplication();
        entity.setId(id);
        entity.setCompanyName(request.getCompanyName().trim());
        entity.setApplyTime(request.getApplyTime());
        entity.setApplyChannel(request.getApplyChannel().trim());
        entity.setPositionName(request.getPositionName().trim());
        entity.setInterviewType(request.getInterviewType() == null ? InterviewType.NONE.getCode() : request.getInterviewType());
        entity.setInterviewScore(request.getInterviewScore());
        entity.setStatus(request.getStatus() == null ? old.getStatus() : request.getStatus());
        entity.setRemark(request.getRemark());

        // 状态机校验：状态发生变化时必须沿合法路径流转
        if (!Objects.equals(old.getStatus(), entity.getStatus())) {
            if (StatusMachine.isTerminal(old.getStatus())
                    || !StatusMachine.canTransition(old.getStatus(), entity.getStatus())) {
                throw BusinessException.invalidStatusTransition();
            }
        }
        validateBusinessRules(entity);
        applicationMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getByIdOrThrow(id);
        applicationMapper.deleteById(id);
    }

    @Override
    public void export(ApplicationQuery query, HttpServletResponse response) {
        List<JobApplication> list = applicationMapper.selectList(buildWrapper(query));
        List<ApplicationExcelRow> rows = new ArrayList<>(list.size());
        for (JobApplication a : list) {
            ApplicationExcelRow row = new ApplicationExcelRow();
            row.setId(a.getId());
            row.setCompanyName(a.getCompanyName());
            row.setApplyTime(a.getApplyTime() == null ? "" : a.getApplyTime().format(EXPORT_TIME_FORMATTER));
            row.setApplyChannel(a.getApplyChannel());
            row.setPositionName(a.getPositionName());
            row.setInterviewTypeLabel(labelOfInterviewType(a.getInterviewType()));
            row.setInterviewScore(a.getInterviewScore());
            row.setStatusLabel(labelOfStatus(a.getStatus()));
            row.setRemark(a.getRemark());
            rows.add(row);
        }

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("投递记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                    StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), ApplicationExcelRow.class)
                    .registerWriteHandler(new AutoColumnWidthHandler())
                    .sheet("投递记录")
                    .doWrite(rows);
        } catch (IOException e) {
            throw new BusinessException(500, "导出失败：" + e.getMessage());
        }
    }

    @Override
    public StatsVO stats() {
        StatsVO vo = new StatsVO();

        // 1. 近 12 周投递量（自然周：周一为一周起点）
        List<StatsVO.WeeklyTrend> weeklyTrend = new ArrayList<>(12);
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        for (int i = 11; i >= 0; i--) {
            LocalDate start = weekStart.minusWeeks(i);
            LocalDate end = start.plusWeeks(1);
            Long count = applicationMapper.selectCount(new LambdaQueryWrapper<JobApplication>()
                    .ge(JobApplication::getApplyTime, start.atStartOfDay())
                    .lt(JobApplication::getApplyTime, end.atStartOfDay()));
            StatsVO.WeeklyTrend trend = new StatsVO.WeeklyTrend();
            trend.setWeek(start.format(WEEK_FORMATTER));
            trend.setCount(count == null ? 0 : count);
            weeklyTrend.add(trend);
        }
        vo.setWeeklyTrend(weeklyTrend);

        // 2. 投递方式分布
        vo.setChannelDist(groupCount("apply_channel"));

        // 3. 状态分布
        vo.setStatusDist(groupCount("status"));

        // 4. 转化漏斗（按当前状态口径）
        StatsVO.Funnel funnel = new StatsVO.Funnel();
        funnel.setApplied(selectCountByCondition(null, null, null));
        funnel.setPending(selectCountByCondition(null, List.of(
                ApplyStatus.PENDING_INTERVIEW.getCode(),
                ApplyStatus.INTERVIEWED.getCode(),
                ApplyStatus.OFFERED.getCode()), null));
        funnel.setInterviewed(selectCountByCondition(null, List.of(
                ApplyStatus.INTERVIEWED.getCode(),
                ApplyStatus.OFFERED.getCode()), null));
        funnel.setOffered(selectCountByCondition(null, List.of(ApplyStatus.OFFERED.getCode()), null));
        vo.setFunnel(funnel);

        return vo;
    }

    // ==================== 私有方法 ====================

    /** 组装查询条件（列表与导出共用） */
    private LambdaQueryWrapper<JobApplication> buildWrapper(ApplicationQuery query) {
        LambdaQueryWrapper<JobApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getCompanyName()), JobApplication::getCompanyName, query.getCompanyName());
        wrapper.like(StringUtils.hasText(query.getPositionName()), JobApplication::getPositionName, query.getPositionName());
        wrapper.eq(StringUtils.hasText(query.getApplyChannel()), JobApplication::getApplyChannel, query.getApplyChannel());
        wrapper.eq(query.getInterviewType() != null, JobApplication::getInterviewType, query.getInterviewType());
        wrapper.eq(query.getStatus() != null, JobApplication::getStatus, query.getStatus());
        wrapper.ge(query.getStartTime() != null, JobApplication::getApplyTime, query.getStartTime());
        wrapper.le(query.getEndTime() != null, JobApplication::getApplyTime, query.getEndTime());
        wrapper.orderByDesc(JobApplication::getApplyTime);
        return wrapper;
    }

    /** 业务规则校验：评分范围、面试形式与状态一致性 */
    private void validateBusinessRules(JobApplication entity) {
        Integer score = entity.getInterviewScore();
        Integer interviewType = entity.getInterviewType() == null ? InterviewType.NONE.getCode() : entity.getInterviewType();
        Integer status = entity.getStatus() == null ? ApplyStatus.APPLIED.getCode() : entity.getStatus();

        if (score != null && (score < 1 || score > 10)) {
            throw new BusinessException(400, "面试评分必须在 1-10 之间");
        }
        // 未面试时不允许有评分
        if (interviewType == InterviewType.NONE.getCode() && score != null) {
            throw new BusinessException(400, "未面试时不能填写面试评分");
        }
        // 已录用 / 已拒绝时，面试形式不允许为"未面试"
        if (StatusMachine.isTerminal(status) && interviewType == InterviewType.NONE.getCode()) {
            throw new BusinessException(400, "已录用 / 已拒绝状态下面试形式不能为未面试");
        }
    }

    private JobApplication getByIdOrThrow(Long id) {
        JobApplication entity = applicationMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound();
        }
        return entity;
    }

    private List<ApplicationVO> toVOList(List<JobApplication> list) {
        List<ApplicationVO> result = new ArrayList<>(list.size());
        for (JobApplication a : list) {
            result.add(toVO(a));
        }
        return result;
    }

    private ApplicationVO toVO(JobApplication a) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(a.getId());
        vo.setCompanyName(a.getCompanyName());
        vo.setApplyTime(a.getApplyTime());
        vo.setApplyChannel(a.getApplyChannel());
        vo.setPositionName(a.getPositionName());
        vo.setInterviewType(a.getInterviewType());
        vo.setInterviewTypeLabel(labelOfInterviewType(a.getInterviewType()));
        vo.setInterviewScore(a.getInterviewScore());
        vo.setStatus(a.getStatus());
        vo.setStatusLabel(labelOfStatus(a.getStatus()));
        vo.setRemark(a.getRemark());
        vo.setCreateTime(a.getCreateTime());
        vo.setUpdateTime(a.getUpdateTime());
        return vo;
    }

    private String labelOfStatus(Integer status) {
        return status == null ? "" : ApplyStatus.of(status).getLabel();
    }

    private String labelOfInterviewType(Integer interviewType) {
        return interviewType == null ? "" : InterviewType.of(interviewType).getLabel();
    }

    /** 按列分组计数，返回 name/value 列表 */
    private List<StatsVO.NameValue> groupCount(String column) {
        List<Map<String, Object>> rows = applicationMapper.selectMaps(
                new QueryWrapper<JobApplication>().select(column + " AS name, COUNT(*) AS value").groupBy(column));
        List<StatsVO.NameValue> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            StatsVO.NameValue nv = new StatsVO.NameValue();
            Object name = row.get("name");
            if (name == null) {
                continue;
            }
            nv.setName(String.valueOf(name));
            nv.setValue(((Number) row.get("value")).longValue());
            result.add(nv);
        }
        return result;
    }

    /** 按状态集合统计数量 */
    private long selectCountByCondition(String channel, List<Integer> statuses, Integer interviewType) {
        LambdaQueryWrapper<JobApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(channel), JobApplication::getApplyChannel, channel);
        wrapper.in(statuses != null && !statuses.isEmpty(), JobApplication::getStatus, statuses);
        wrapper.eq(interviewType != null, JobApplication::getInterviewType, interviewType);
        Long count = applicationMapper.selectCount(wrapper);
        return count == null ? 0 : count;
    }
}
