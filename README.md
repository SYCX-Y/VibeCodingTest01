# 简历投递记录系统

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.7-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1)
![EasyExcel](https://img.shields.io/badge/EasyExcel-3.3.4-green)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.7.0-409EFF)
![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36)
![License](https://img.shields.io/badge/License-MIT-yellow)

一个面向个人求职者的简历投递记录与求职过程管理工具：统一记录每次投递（公司、时间、渠道、岗位、面试形式、面试评分），支持多条件筛选搜索、状态流转管理、统计看板与 Excel 导出，帮助复盘投递策略、跟进面试进度。

- 后端：Spring Boot 3 + MyBatis-Plus + MySQL + EasyExcel
- 前端：Vue 3 + Element Plus（纯静态资源，内嵌于 Spring Boot，无需 Node 环境）

## 功能特性

- **投递记录增删改查**：新增、分页查询、详情、修改、删除
- **多条件筛选搜索**：公司名 / 岗位名模糊搜索，投递方式、面试形式、状态精确筛选，投递时间范围过滤
- **状态机管理**：内置合法流转校验，防止非法状态变更
- **统计看板**：近 12 周投递量趋势、投递方式分布、状态分布、转化漏斗
- **Excel 导出**：按当前筛选条件一键导出 `.xlsx`（EasyExcel + 自适应列宽）
- **面试信息记录**：面试形式（线上 / 线下）与面试评分（1-10）
- **统一响应与全局异常处理**：`{ code, message, data }` 规范返回

## 项目结构

```
job-application-system/
├── backend/                          # Spring Boot 后端（前端页面内嵌于 static）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/jobapplication/
│       │   ├── JobApplicationSystemApplication.java   # 启动类
│       │   ├── common/                                # 统一响应 / 异常处理 / 状态机 / Excel 列宽
│       │   ├── config/                                # MyBatis-Plus 分页 / CORS / 时间格式
│       │   ├── controller/                            # 接口层
│       │   ├── dto/                                   # 请求 / 响应 / 导出 / 统计对象
│       │   ├── entity/                                # 实体（job_application 表）
│       │   ├── enums/                                 # 投递状态、面试形式枚举
│       │   ├── mapper/                                # MyBatis-Plus Mapper
│       │   └── service/                               # 业务层（状态机校验、导出、统计）
│       └── resources/
│           ├── application.yml                       # 服务端口 / 数据库等配置
│           └── static/                               # 前端页面（index.html / css / js）
├── sql/
│   └── schema.sql                                    # 建库建表 + 23 条示例数据
└── README.md
```

## 快速开始

### 环境要求

- JDK 17 及以上
- Maven 3.6+
- MySQL 8.0+（默认端口 3306）

### 1. 初始化数据库

在项目根目录执行（或通过 Navicat / IDEA Database 面板导入）：

```bash
mysql -u root -p < sql/schema.sql
```

脚本会自动创建 `job_application` 库和 `job_application` 表，并插入 23 条示例数据。

### 2. 修改数据库配置

编辑 `backend/src/main/resources/application.yml`：

- 服务端口：`server.port`，默认 **8082**
- 数据库账号：`spring.datasource.username`，默认 `root`
- 数据库密码：通过环境变量 `SQL_PWD` 设置（推荐），或直接修改 `spring.datasource.password`

```bash
# 设置环境变量示例（Windows PowerShell）
$env:SQL_PWD = "你的MySQL密码"
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

或用 IDEA 打开 `backend` 目录，直接运行 `JobApplicationSystemApplication` 的 main 方法。

### 4. 访问系统

浏览器打开 http://localhost:8082

- 「投递记录」页：增删改查、筛选搜索、导出 Excel
- 「统计看板」页：近 12 周投递趋势、投递方式分布、状态分布、转化漏斗

## API 接口

基础路径：`/api/applications`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/applications` | 新增投递记录 |
| GET | `/api/applications` | 分页查询（筛选：companyName / positionName / applyChannel / interviewType / status / startTime / endTime） |
| GET | `/api/applications/{id}` | 查询详情 |
| PUT | `/api/applications/{id}` | 修改记录（含状态机校验） |
| DELETE | `/api/applications/{id}` | 删除记录 |
| GET | `/api/applications/export` | 按当前筛选条件导出 Excel（.xlsx） |
| GET | `/api/applications/stats` | 统计看板数据 |

统一响应：`{ "code": 0, "message": "success", "data": ... }`，`code = 0` 表示成功；业务错误码 `40001` 表示非法状态流转。

## 状态机与业务规则

投递状态：`已投递(0) → 待面试(1) → 已面试(2) → 已录用(3)`，其中 `已拒绝(4)`、`已放弃(5)` 可在前置阶段退出

```
已投递  →  待面试  →  已面试  →  已录用
  │         │         │
  ├→ 已拒绝 ←┘         ├→ 已拒绝
  └→ 已放弃 ──────────  └→ 已放弃
```

- 终态（已录用 / 已拒绝 / 已放弃）不可再修改状态
- 必填：公司名、投递方式、投递岗位；投递时间不填默认当前时间
- 面试评分：1-10 的整数；未面试时不允许填写评分
- 已录用 / 已拒绝状态下，面试形式不能为「未面试」

## 统计口径

- **近 12 周投递量**：自然周（周一为一周起点）统计投递时间落在该周的记录数
- **转化漏斗**：投递 = 全部记录；待面试 = 状态为待面试 / 已面试 / 已录用；已面试 = 已面试 / 已录用；已录用 = 已录用

## 常见问题

**Q：启动报数据库连接失败？**
检查 MySQL 是否启动、`application.yml` 中的账号密码是否正确（或 `SQL_PWD` 环境变量是否设置），并确认已执行 `schema.sql`。

**Q：页面打不开 / 图表不显示？**
前端依赖 CDN（jsdelivr）加载 Element Plus 等资源，首次访问需要联网；如网络受限，请将 `index.html` 与 `app.js` 中引用的 CDN 地址替换为可访问的镜像。

**Q：想清空示例数据？**
执行 `DELETE FROM job_application;` 即可，id 会继续自增。

**Q：想独立部署前端？**
前端为纯静态资源，将 `static/` 目录部署到任意静态服务器即可；后端已配置 CORS（`/api/**` 允许跨域）。

## License

MIT License（如使用其他协议，请自行修改本文件与徽章）。
