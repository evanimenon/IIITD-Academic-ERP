# IIITD Academic ERP – CSE201 Advanced Programming Project

A desktop Academic ERP system for IIIT-Delhi that supports students, instructors, and administrators: course registration, grading, and basic maintenance tooling – implemented as a Java Swing application backed by MySQL and JDBC connection pooling. 

**Made by**
* Evani Menon – 2024210
* Nandika Routray – 2024371

**Course:** CSE201 – Advanced Programming Final Course Project

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

---

## Project Structure

```text
.
├── src
│   └── erp
│       ├── Main.java
│       ├── auth/              # Login, roles, AuthContext
│       ├── db/                # DatabaseConnection, HikariCP pool, DAOs, MaintenanceService
│       ├── models/            # POJOs for users, courses, sections, enrollments, grades
│       ├── tools/             # Utilities (e.g., CSV importers)
│       └── ui/
│           ├── common/        # Shared UI components (RoundedPanel, FontKit, NavButton…)
│           ├── auth/          # LoginPage and auth frame
│           ├── student/       # Student dashboards, course catalog, timetable, grades
│           ├── instructor/    # Instructor dashboards, grading, class stats
│           └── admin/         # Admin dashboards, manage users/courses/sections, maintenance
├── resources
│   ├── fonts/                 # Inter and other fonts
│   ├── images/                # Logos, icons, UI artwork
│   └── db.properties          # DB configuration (copied into out/)
├── data
│   ├── *.csv                  # Seed CSVs for users / demo data
│   └── *.sql                  # Schema + seed SQL for auth_db and erp_db
├── lib                        # External JARs (JDBC driver, HikariCP, etc.)
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

<img width="1496" height="934" alt="Screenshot 2025-11-27 at 17 21 41" src="https://github.com/user-attachments/assets/4b25d8a7-defb-4618-a064-eb416cd33ec6" />

* Central **login screen** for all users.
* Role-based redirection to **Admin**, **Instructor**, or **Student** dashboards.
* Auth data stored in `auth_db.users_auth` with secure password handling.

### 2. Student Portal

* **Dashboard overview** with welcome meta, current semester and maintenance banner.
  
* **Course Catalog & Registration**

  * Browse all offered courses with code, acronym, title, and credits.
  * Register / drop sections (subject to:

    * global **course drop deadline** from `settings.COURSE_DROP_DEADLINE`
    * **maintenance mode** – registration is read-only when enabled).
  * Registered courses visually pinned to the top and marked with a badge.
  
<img width="1496" height="903" alt="Screenshot 2025-11-27 at 17 22 19" src="https://github.com/user-attachments/assets/cb28f3a9-4377-46e5-939c-647d9281cd5a" />

* **My Timetable**

  * Weekly timetable grid by day/time.
  * Cells show **course acronym** + section.
<img width="1496" height="934" alt="Screenshot 2025-11-27 at 17 23 49" src="https://github.com/user-attachments/assets/049963d9-8f52-4fdb-ade7-597f46f40d40" />

   
* **My Grades**

  * Per-course grade view using the `grades` table.
  * Read-only; driven directly from instructor entries.
  
<img width="1280" height="827" alt="image" src="https://github.com/user-attachments/assets/d8b22724-838f-47fb-8315-93b8b15c8a7f" />

### 3. Instructor Portal

* **My Sections**

  * Cards for each section taught (course, semester, room, capacity, time).
* **Grade Students**

  * View enrolled students per section.
  * Enter / update grades, with inline validation.
  * Saves to `grades` table; uses transactions to keep data consistent.
* **Class Stats (optional panel)**

  * Simple aggregate statistics per section (e.g., counts / distribution).
* **Maintenance awareness**

  * Shared banner: when maintenance mode is ON, all write actions (grading etc.)
    are disabled, but instructors can still view data.
<img width="1280" height="827" alt="image" src="https://github.com/user-attachments/assets/e7a341d9-206e-4c7d-a728-3841c285dd03" />


### 4. Admin Portal

* **User Management**

  * CRUD for **students**, **instructors**, and login accounts in `users_auth`.
  * Import helpers via CSV (e.g., `erp.tools.ImportUsersCsv`).
<img width="1600" height="1034" alt="image" src="https://github.com/user-attachments/assets/50f21495-f503-43df-8094-28d512573d1e" />

* **Manage Courses & Sections**

  * Grid of all courses (ID, acronym, title, credits).
  * For a selected course:

    * List of **sections** with instructor, room, capacity, semester, year, time.
    * Editable fields for section metadata (room, capacity, time, instructor).
  * **Enrollment management**

    * Table of enrolled students for the selected section.
    * Add student to section, remove student from section.
    * Changes staged in memory and committed via a prominent **“Save Changes”**
      button anchored at the bottom-right of the panel.
<img width="1600" height="1034" alt="image" src="https://github.com/user-attachments/assets/7054bad6-dcb6-46be-b615-47be414ba474" />

* **Global Settings**

  * Maintenance mode (see below).
  * Course drop deadline and other global flags stored in the `settings` table.
* **Maintenance & Backup**

  * Dedicated **Maintenance & Backup** page:

    * Toggle **maintenance mode** (ON/OFF) – persisted as
      `settings.maintenance_mode = 'ON'/'OFF'`.
    * When ON, all student/instructor UIs show a banner and become read-only.
    * Export CSV snapshots of core tables:
      `users_auth, students, instructors, courses, sections, enrollments, grades`.
    * Optional “export selected tables” dialog with multi-select.

### 5. Maintenance Mode (System-wide)

* Centralized service `MaintenanceService`:

  * `isMaintenanceOn()` reads from `erp_db.settings`.
  * `setMaintenance(boolean)` writes/updates the setting with
    `maintenance_mode = 'ON'/'OFF'`.
* Every main frame (Admin, Student, Instructor) checks this flag on load and when
  switching pages:

  * Shows a **colored banner** when maintenance is ON.
  * Disables all actions that mutate data (registration, grading, edits).
* The admin toggle provides confirmation dialogs and user feedback
  (“Maintenance mode is now ON/OFF”).
<img width="1600" height="1034" alt="image" src="https://github.com/user-attachments/assets/02952de1-a060-4a00-bf86-07b42e335273" />

### 6. Tools & Utilities

* **CSV importers** (in `erp.tools.*`) to bulk-load initial data.
* Shared UI components:

  * **Rounded panels/buttons**, consistent **Inter** typography,
  * Reusable nav bar with hover effects,
  * Small helper dialogs for errors, confirmations, and info messages.

---

## Tech Stack

* **Language:** Java (JDK compatible with the course setup, e.g. Java 17).
* **Desktop UI:** Java Swing + custom components (RoundedPanel, RoundedButton, etc.).
* **Database:** MySQL 8 (`auth_db`, `erp_db`).
* **Persistence & Connectivity:**

  * JDBC (MySQL Connector/J driver in `lib/`).
  * HikariCP connection pooling via `DatabaseConnection` helper.
* **Data Layer:**

  * Normalized relational schema with foreign keys and cascading behavior for:
    `courses`, `sections`, `students`, `instructors`,
    `enrollments`, `grades`, `settings`.
* **Build / Run:**

  * Plain `javac` + `java` commands, wired via simple shell / PowerShell scripts.
* **Misc:**

  * CSV exports via plain Java IO (`FileWriter`, `ResultSetMetaData`) from
    the **Maintenance** panel.
