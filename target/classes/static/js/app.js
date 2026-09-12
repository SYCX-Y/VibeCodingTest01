/**
 * 简历投递记录系统 - 前端逻辑（Vue3 组合式 API + Element Plus + ECharts）
 */
(function () {
    const { createApp, ref, reactive, computed, onMounted, watch, nextTick } = Vue;
    const { ElMessage, ElMessageBox } = ElementPlus;

    // ============ 常量 ============
    const STATUS_OPTIONS = [
        { value: 0, label: '已投递' },
        { value: 1, label: '待面试' },
        { value: 2, label: '已面试' },
        { value: 3, label: '已录用' },
        { value: 4, label: '已拒绝' },
        { value: 5, label: '已放弃' }
    ];
    const STATUS_TAG = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger', 5: 'info' };
    const CHANNEL_OPTIONS = ['BOSS直聘', '智联招聘', '前程无忧', '官网', '内推', '其他'];
    const INTERVIEW_OPTIONS = [
        { value: 0, label: '未面试' },
        { value: 1, label: '线上面试' },
        { value: 2, label: '线下面试' }
    ];
    const TERMINAL_STATUS = [3, 4, 5];

    // ============ 应用 ============
    const App = {
        setup() {
            // ---- 状态 ----
            const activeTab = ref('list');
            const list = ref([]);
            const total = ref(0);
            const pendingCount = ref(0);
            const loading = ref(false);
            const dialogVisible = ref(false);
            const isEdit = ref(false);
            const submitting = ref(false);
            const formRef = ref(null);

            const query = reactive({
                companyName: '',
                positionName: '',
                applyChannel: '',
                interviewType: null,
                status: null,
                dateRange: [],
                page: 1,
                pageSize: 10
            });

            // 当前选中的行（用于表格下方独立操作条）
            const currentRow = ref(null);

            function handleCurrentChange(row) {
                currentRow.value = row || null;
            }

            function clearCurrent() {
                currentRow.value = null;
            }

            const emptyForm = () => ({
                id: null,
                companyName: '',
                applyTime: '',
                applyChannel: '',
                positionName: '',
                interviewType: 0,
                interviewScore: null,
                status: 0,
                remark: ''
            });
            const form = reactive(emptyForm());

            const rules = {
                companyName: [{ required: true, message: '请输入公司名', trigger: 'blur' }],
                applyChannel: [{ required: true, message: '请选择投递方式', trigger: 'change' }],
                positionName: [{ required: true, message: '请输入投递岗位', trigger: 'blur' }]
            };

            const isTerminal = computed(() => TERMINAL_STATUS.includes(form.status));

            // ---- API 封装 ----
            async function request(url, options = {}) {
                const res = await fetch(url, {
                    headers: { 'Content-Type': 'application/json' },
                    ...options
                });
                let json;
                try {
                    json = await res.json();
                } catch (e) {
                    throw new Error('服务器响应解析失败');
                }
                if (json.code !== 0) {
                    const msg = json.message || '请求失败';
                    ElMessage.error(msg);
                    throw new Error(msg);
                }
                return json.data;
            }

            function buildQueryString() {
                const p = new URLSearchParams();
                if (query.companyName) p.set('companyName', query.companyName);
                if (query.positionName) p.set('positionName', query.positionName);
                if (query.applyChannel) p.set('applyChannel', query.applyChannel);
                if (query.interviewType !== null && query.interviewType !== undefined && query.interviewType !== '') {
                    p.set('interviewType', query.interviewType);
                }
                if (query.status !== null && query.status !== undefined && query.status !== '') {
                    p.set('status', query.status);
                }
                if (query.dateRange && query.dateRange.length === 2) {
                    p.set('startTime', query.dateRange[0] + ' 00:00:00');
                    p.set('endTime', query.dateRange[1] + ' 23:59:59');
                }
                p.set('page', query.page);
                p.set('pageSize', query.pageSize);
                return p.toString();
            }

            // ---- 列表 ----
            async function loadList() {
                loading.value = true;
                try {
                    const data = await request('/api/applications?' + buildQueryString());
                    list.value = data.records || [];
                    total.value = data.total || 0;
                    // 数据刷新后清空选中行，避免操作条指向已不在列表的记录
                    currentRow.value = null;
                } catch (e) { /* 已提示 */ } finally {
                    loading.value = false;
                }
            }

            function search() {
                query.page = 1;
                loadList();
            }

            function resetQuery() {
                Object.assign(query, {
                    companyName: '', positionName: '', applyChannel: '',
                    interviewType: null, status: null, dateRange: [],
                    page: 1, pageSize: query.pageSize
                });
                loadList();
            }

            function onSizeChange() {
                query.page = 1;
                loadList();
            }

            // ---- 新增 / 编辑 ----
            function openCreate() {
                isEdit.value = false;
                Object.assign(form, emptyForm());
                dialogVisible.value = true;
            }

            function openEdit(row) {
                isEdit.value = true;
                Object.assign(form, {
                    id: row.id,
                    companyName: row.companyName,
                    applyTime: row.applyTime,
                    applyChannel: row.applyChannel,
                    positionName: row.positionName,
                    interviewType: row.interviewType,
                    interviewScore: row.interviewScore,
                    status: row.status,
                    remark: row.remark || ''
                });
                dialogVisible.value = true;
            }

            async function submit() {
                try {
                    await formRef.value.validate();
                } catch (e) {
                    return;
                }
                // 未面试时清空评分
                const payload = { ...form };
                if (payload.interviewType === 0) {
                    payload.interviewScore = null;
                }
                if (!payload.applyTime) {
                    payload.applyTime = dayjs().format('YYYY-MM-DD HH:mm:ss');
                }
                submitting.value = true;
                try {
                    if (isEdit.value) {
                        await request('/api/applications/' + payload.id, {
                            method: 'PUT',
                            body: JSON.stringify(payload)
                        });
                        ElMessage.success('修改成功');
                    } else {
                        await request('/api/applications', {
                            method: 'POST',
                            body: JSON.stringify(payload)
                        });
                        ElMessage.success('新增成功');
                    }
                    dialogVisible.value = false;
                    loadList();
                } catch (e) { /* 已提示 */ } finally {
                    submitting.value = false;
                }
            }

            // ---- 删除 ----
            function removeRow(row) {
                ElMessageBox.confirm(
                    `确定删除「${row.companyName} - ${row.positionName}」这条投递记录吗？删除后不可恢复。`,
                    '删除确认',
                    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
                ).then(async () => {
                    try {
                        await request('/api/applications/' + row.id, { method: 'DELETE' });
                        ElMessage.success('已删除');
                        loadList();
                    } catch (e) { /* 已提示 */ }
                }).catch(() => {});
            }

            // ---- 导出 ----
            function exportExcel() {
                window.location.href = '/api/applications/export?' + buildQueryString();
            }

            // ---- 统计看板 ----
            const weeklyChartEl = ref(null);
            const channelChartEl = ref(null);
            const statusChartEl = ref(null);
            const funnelChartEl = ref(null);
            const statsData = reactive({
                weeklyTrend: [],
                channelDist: [],
                statusDist: [],
                funnel: { applied: 0, pending: 0, interviewed: 0, offered: 0 }
            });
            const charts = [];

            const statsEmpty = computed(() => {
                const totalCount = statsData.funnel.applied || 0;
                return totalCount === 0;
            });

            function renderCharts() {
                renderWeeklyChart();
                renderChannelChart();
                renderStatusChart();
                renderFunnelChart();
            }

            function initChart(elRef) {
                if (!elRef.value) return null;
                return echarts.init(elRef.value);
            }

            function renderWeeklyChart() {
                const chart = initChart(weeklyChartEl);
                if (!chart) return;
                charts.push(chart);
                const trend = statsData.weeklyTrend || [];
                chart.setOption({
                    tooltip: { trigger: 'axis' },
                    grid: { left: 45, right: 20, top: 30, bottom: 35 },
                    xAxis: {
                        type: 'category',
                        data: trend.map(t => t.week),
                        axisLabel: { rotate: 30, color: '#606266' }
                    },
                    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { type: 'dashed' } } },
                    series: [{
                        name: '投递量',
                        type: 'bar',
                        data: trend.map(t => t.count),
                        barMaxWidth: 32,
                        itemStyle: { color: '#5a6cf5', borderRadius: [4, 4, 0, 0] },
                        label: { show: true, position: 'top', color: '#909399' }
                    }]
                });
            }

            function renderChannelChart() {
                const chart = initChart(channelChartEl);
                if (!chart) return;
                charts.push(chart);
                const dist = statsData.channelDist || [];
                chart.setOption({
                    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
                    legend: { bottom: 0, type: 'scroll' },
                    series: [{
                        name: '投递方式',
                        type: 'pie',
                        radius: ['38%', '62%'],
                        center: ['50%', '45%'],
                        avoidLabelOverlap: true,
                        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
                        label: { formatter: '{b}: {c}' },
                        data: dist.map(d => ({ name: d.name, value: d.value }))
                    }]
                });
            }

            function renderStatusChart() {
                const chart = initChart(statusChartEl);
                if (!chart) return;
                charts.push(chart);
                const dist = statsData.statusDist || [];
                const colorMap = { 0: '#909399', 1: '#e6a23c', 2: '#409eff', 3: '#67c23a', 4: '#f56c6c', 5: '#b1b3b8' };
                const nameMap = {};
                STATUS_OPTIONS.forEach(o => { nameMap[o.value] = o.label; });
                chart.setOption({
                    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
                    legend: { bottom: 0, type: 'scroll' },
                    series: [{
                        name: '状态',
                        type: 'pie',
                        radius: ['40%', '65%'],
                        center: ['50%', '45%'],
                        roseType: 'radius',
                        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
                        label: { formatter: '{b}: {c}' },
                        data: dist.map(d => ({
                            name: nameMap[d.name] || d.name,
                            value: d.value,
                            itemStyle: { color: colorMap[d.name] || '#909399' }
                        }))
                    }]
                });
            }

            function renderFunnelChart() {
                const chart = initChart(funnelChartEl);
                if (!chart) return;
                charts.push(chart);
                const f = statsData.funnel || {};
                chart.setOption({
                    tooltip: { trigger: 'item', formatter: '{b}: {c} 条' },
                    color: ['#5a6cf5', '#409eff', '#67c23a', '#e6a23c'],
                    series: [{
                        name: '转化漏斗',
                        type: 'funnel',
                        left: '12%',
                        width: '76%',
                        top: 10,
                        bottom: 20,
                        minSize: '20%',
                        sort: 'descending',
                        gap: 4,
                        label: { show: true, position: 'inside', formatter: '{b}\n{c} 条' },
                        data: [
                            { value: f.applied || 0, name: '投递' },
                            { value: f.pending || 0, name: '待面试' },
                            { value: f.interviewed || 0, name: '已面试' },
                            { value: f.offered || 0, name: '已录用' }
                        ]
                    }]
                });
            }

            async function loadStats() {
                try {
                    const data = await request('/api/applications/stats');
                    pendingCount.value = (data.funnel && data.funnel.pending) || 0;
                    Object.assign(statsData, {
                        weeklyTrend: data.weeklyTrend || [],
                        channelDist: data.channelDist || [],
                        statusDist: data.statusDist || [],
                        funnel: data.funnel || { applied: 0, pending: 0, interviewed: 0, offered: 0 }
                    });
                    await nextTick();
                    // 清空旧图表实例，重新渲染
                    charts.forEach(c => c.dispose());
                    charts.length = 0;
                    renderCharts();
                } catch (e) { /* 已提示 */ }
            }

            // ---- 生命周期 ----
            watch(activeTab, (val) => {
                if (val === 'stats') {
                    loadStats();
                }
            });

            onMounted(() => {
                loadList();
                loadStats();
                window.addEventListener('resize', () => {
                    charts.forEach(c => c.resize());
                });
            });

            return {
                activeTab, list, total, pendingCount, loading, dialogVisible, isEdit, submitting, formRef,
                query, form, rules, isTerminal,
                currentRow, handleCurrentChange, clearCurrent,
                STATUS_OPTIONS, STATUS_TAG, CHANNEL_OPTIONS, INTERVIEW_OPTIONS,
                search, resetQuery, onSizeChange, loadList,
                openCreate, openEdit, submit, removeRow, exportExcel,
                weeklyChartEl, channelChartEl, statusChartEl, funnelChartEl,
                statsData, statsEmpty
            };
        }
    };

    createApp(App)
        .use(ElementPlus, { locale: window.ElementPlusLocaleZhCn })
        .mount('#app');
})();
