# IIITD Academic ERP – CSE201 Advanced Programming Project

A desktop Academic ERP system for IIIT-Delhi supporting students, instructors, and administrators: course registration, grading, timetable generation, and maintenance tooling — implemented as a **Java Swing** application backed by **MySQL** with **JDBC + HikariCP** connection pooling.

**Made by**

* **Evani Menon – 2024210**
* **Nandika Routray – 2024371**

**Course:** CSE201 – Advanced Programming (Final Course Project)

---

## How to Compile & Run

```bash
### macOS / Linux
javac -cp "lib/*" -d out $(find src -name "*.java")
cp -R src/resources/* out/
java -cp "out:lib/*:src:src/resources" erp.Main


### Windows (cmd.exe alternative)
for /R src %f in (*.java) do @echo %f >> files.txt
javac -cp ".;lib/*" -d bin @files.txt
java -cp "bin;lib/*" erp.Main
```

---

## Database Setup (MySQL 8)

The project uses **two databases**:

* `auth_db` — login accounts
* `erp_db` — students, instructors, courses, sections, components, enrollments, grades, settings

Pre-seeded SQL dumps are located in the **`data/`** folder:

- `data/auth_seed.sql`
- `data/erp_seed.sql`
---

### 1. Create and seed the databases

From project root:

```bash

# Seed authentication DB (auth_db)
mysql -u root -p < data/auth_seed.sql

# Seed main ERP DB (erp_db)
mysql -u root -p < data/erp_seed.sql
```

Each script will:

* Drop the database if it exists
* Recreate tables
* Load data from the CSV files in `/data`
* Set up foreign keys & indexes

---

### 2. Configure database credentials

Edit:

```
src/resources/db.properties
```

(This file is copied into `out/` automatically during compile.)

Typical configuration:

```properties
auth.url=jdbc:mysql://localhost:3306/auth_db
auth.user=root
auth.password=your_password

erp.url=jdbc:mysql://localhost:3306/erp_db
erp.user=root
erp.password=your_password
```

Replace `your_password` with your MySQL password.
If using a non-standard MySQL port, update the URLs accordingly.

---

### 4. Ensure MySQL is running before launching the app.

---

## Project Structure

```text
.
├── src
│   └── erp
│       ├── Main.java
│       ├── auth/              # Login, roles, AuthContext
│       ├── db/                # Hikari pools, DatabaseConnection, DAOs, MaintenanceService
│       ├── models/            # POJOs for users, courses, sections, enrollments, grades
│       ├── tools/             # CSV import/export utilities
│       └── ui/
│           ├── common/        # Shared UI widgets (RoundedPanel, FontKit, NavButton…)
│           ├── auth/          # Login UI
│           ├── student/       # Student dashboard, catalog, timetable, grades
│           ├── instructor/    # Instructor dashboard, grading, class stats
│           └── admin/         # Admin dashboard: users/courses/sections, enrollment, maintenance
├── resources
│   ├── fonts/                 # Inter font family
│   ├── images/                # Icons and UI assets
│   └── db.properties          # DB configuration (copied into out/)
├── data
│   ├── *.csv                  # Seed CSVs for all tables
│   └── *.sql                  # SQL seeds for auth_db & erp_db
├── lib                        # External JARs (MySQL Connector/J, HikariCP)
├── out/                       # Compiled classes + copied resources (generated)
└── README.md
```

---

## Demo Accounts

| Role       | Username | Password  |
| ---------- | -------- | --------- |
| Admin      | `admin1` | `temp123` |
| Instructor | `inst1`  | `temp123` |
| Student    | `stu1`   | `temp123` |
| Student    | `stu2`   | `temp123` |

---

## Features

### 1. Authentication & Roles

* Central login screen
* Role-based dashboards
* Secure credential storage
* Values loaded from `auth_db.users_auth`

---

### 2. Student Portal

#### Dashboard

* Welcome banner, student info, semester
* Maintenance mode banner when system is locked

#### Course Catalog & Registration

* Browse available courses

* Register/drop sections

* Global controls (via `settings` table):

  * course drop deadline
  * maintenance mode

* Registered courses:

  * Displayed **first**
  * Highlighted with a **green badge**

#### My Timetable

* Weekly timetable grid
* Cells display: **Course acronym + Section**

#### My Grades

* Component-level grade breakdown
* Final grade computed from instructor-entered components

---

### 3. Instructor Portal

#### My Sections

* Cards for each section taught
* Course, semester, timings, room, capacity

#### Grade Students

* Per-section student list

* Grade entry

* Uses:

  * `grades` table
  * `section_components` for weightage

* Auto-updates final grade

#### Class Stats

* Basic statistics for grade distribution

---

### 4. Admin Portal

#### User Management

* Manage students & instructors
* Create login accounts in `users_auth`
* CSV-based import

#### Manage Courses & Sections

* Full course list
* For a selected course:

  * All sections shown
  * Editable metadata:

    * Room
    * Capacity
    * Instructor
    * Day/Time

#### Enrollment Management

* View enrolled students for a section
* Add/remove enrollments
* Changes saved via a “Save Changes” action

#### Global Settings

* Course drop deadline
* Maintenance mode toggle

#### Maintenance & Backup

* Toggle maintenance mode (ON/OFF)
* Export CSV snapshots for all major tables

---

### 5. System-wide Maintenance Mode

* Controlled via `erp_db.settings`
* When ON:

  * All write actions (registration, grading, editing) are disabled
  * Visible banner across all dashboards
* Admin can toggle it from Maintenance Panel

---

### 6. Tools & Utilities

* CSV importers (`erp.tools.*`)
* Custom Swing components (RoundedPanel, RoundedButton, FontKit)
* Export utilities via `FileWriter`
* Helper dialogs for confirmation/error/info

---

## Tech Stack

* **Java (JDK 17)**
* **Swing UI**
* **MySQL 8**
* **JDBC + HikariCP connection pooling**
* **CSV-based seeding (LOAD DATA LOCAL INFILE)**
* **Plain javac/java build pipeline**
