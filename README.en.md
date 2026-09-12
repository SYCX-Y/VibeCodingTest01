# Job Application Record System

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.7-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1)
![EasyExcel](https://img.shields.io/badge/EasyExcel-3.3.4-green)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.7.0-409EFF)
![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36)
![License](https://img.shields.io/badge/License-MIT-yellow)

A personal job application tracker that helps you record every application (company, date, channel, position, interview type, interview score), filter and search records, manage status transitions, view statistics, and export to Excel — so you can follow up on interviews and review your job-hunting strategy.

- Backend: Spring Boot 3 + MyBatis-Plus + MySQL + EasyExcel
- Frontend: Vue 3 + Element Plus (pure static assets embedded in Spring Boot, no Node.js required)

## Features

- **Full CRUD**: create, paginate, view detail, update and delete application records
- **Multi-condition filtering**: fuzzy search by company / position name, exact filter by channel, interview type and status, plus apply-time range
- **Status machine**: built-in legal transition validation prevents invalid status changes
- **Statistics dashboard**: 12-week application trend, channel distribution, status distribution, conversion funnel
- **Excel export**: one-click export of current filtered results to `.xlsx` (EasyExcel + auto column width)
- **Interview tracking**: interview type (online / onsite) and score (1-10)
- **Unified response & global exception handling**: `{ code, message, data }` convention

## Project Structure

```
job-application-system/
├── backend/                          # Spring Boot backend (frontend embedded in static)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/jobapplication/
│       │   ├── JobApplicationSystemApplication.java   # Application entry
│       │   ├── common/                                # Response / exception / status machine / Excel width
│       │   ├── config/                                # MyBatis-Plus pagination / CORS / time format
│       │   ├── controller/                            # REST controllers
│       │   ├── dto/                                   # Request / response / export / stats DTOs
│       │   ├── entity/                                # Entity (job_application table)
│       │   ├── enums/                                 # Status & interview type enums
│       │   ├── mapper/                                # MyBatis-Plus mappers
│       │   └── service/                               # Business layer (state machine, export, stats)
│       └── resources/
│           ├── application.yml                       # Server port / datasource config
│           └── static/                               # Frontend pages (index.html / css / js)
├── sql/
│   └── schema.sql                                    # Database & table DDL + 23 sample rows
└── README.md
```

## Getting Started

### Prerequisites

- JDK 17+
- Maven 3.6+
- MySQL 8.0+ (default port 3306)

### 1. Initialize the Database

Run from the project root (or import via Navicat / IDEA Database panel):

```bash
mysql -u root -p < sql/schema.sql
```

The script creates the `job_application` database and table, and inserts 23 sample rows.

### 2. Configure the Datasource

Edit `backend/src/main/resources/application.yml`:

- Server port: `server.port`, default **8082**
- Username: `spring.datasource.username`, default `root`
- Password: set the environment variable `SQL_PWD` (recommended), or edit `spring.datasource.password` directly

```bash
# Windows PowerShell example
$env:SQL_PWD = "your-mysql-password"
```

### 3. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

Or open the `backend` directory in IDEA and run the `main` method of `JobApplicationSystemApplication`.

### 4. Access the System

Open http://localhost:8082 in your browser.

- "Applications" tab: CRUD, filter/search, export to Excel
- "Dashboard" tab: 12-week trend, channel distribution, status distribution, conversion funnel

## REST API

Base path: `/api/applications`

| Method | Path | Description |
|---|---|---|
| POST | `/api/applications` | Create an application record |
| GET | `/api/applications` | Paginated query (filters: companyName / positionName / applyChannel / interviewType / status / startTime / endTime) |
| GET | `/api/applications/{id}` | Get record detail |
| PUT | `/api/applications/{id}` | Update record (with status-machine validation) |
| DELETE | `/api/applications/{id}` | Delete record |
| GET | `/api/applications/export` | Export filtered results to Excel (.xlsx) |
| GET | `/api/applications/stats` | Dashboard statistics |

Unified response: `{ "code": 0, "message": "success", "data": ... }`; `code = 0` means success, business error `40001` means invalid status transition.

## Status Machine & Business Rules

Status flow: `Applied(0) -> Pending Interview(1) -> Interviewed(2) -> Offered(3)`, with `Rejected(4)` and `Abandoned(5)` as exits from earlier stages.

```
Applied  ->  Pending Interview  ->  Interviewed  ->  Offered
  |               |                    |
  +-> Rejected <--+                    +-> Rejected
  +-> Abandoned ---------------------> +-> Abandoned
```

- Terminal states (Offered / Rejected / Abandoned) can no longer change status
- Required fields: company name, apply channel, position name; apply time defaults to now if omitted
- Interview score: integer 1-10; score is not allowed when not interviewed
- In Offered / Rejected states, interview type must not be "Not Interviewed"

## Statistics Definition

- **12-week trend**: counts records whose apply time falls in each natural week (Monday as week start)
- **Conversion funnel**: Applied = all records; Pending = status in (Pending Interview, Interviewed, Offered); Interviewed = (Interviewed, Offered); Offered = Offered

## FAQ

**Q: Database connection fails on startup?**
Check whether MySQL is running, whether the username/password in `application.yml` is correct (or `SQL_PWD` is set), and whether `schema.sql` has been executed.

**Q: The page or charts don't render?**
The frontend loads Element Plus etc. from CDN (jsdelivr) — internet access is required on first visit. If the network is restricted, replace the CDN URLs in `index.html` and `app.js` with an accessible mirror.

**Q: How to clear sample data?**
Run `DELETE FROM job_application;` — the auto-increment id will continue from where it left off.

**Q: Can the frontend be deployed separately?**
Yes. The frontend is pure static — deploy the `static/` directory to any static server; the backend already enables CORS for `/api/**`.

## License

MIT License (switch to another license by updating this file and the badge accordingly).
