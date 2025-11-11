-- bootstrap.sql — ERP DB (portable, CSV-driven)
-- MySQL 8.x recommended

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS erp_db;
CREATE DATABASE erp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE erp_db;

-- ────────────────────────────────────────────────────────────────────────────
-- USERS (external) NOTE:
-- Your auth DB exists separately. We keep a FK only for INSTRUCTORS since
-- those IDs are BIGINTs matching auth_db.users_auth.user_id in your screenshots.
-- Students use alphanumeric IDs → do NOT FK them to auth_db.
-- ────────────────────────────────────────────────────────────────────────────

-- STUDENTS  (IDs like 'aadi24001', etc.)
CREATE TABLE students (
  student_id VARCHAR(64)  NOT NULL,
  roll_no    VARCHAR(32)  NOT NULL,
  full_name  VARCHAR(128) NULL,
  program    VARCHAR(64)  NOT NULL,
  year       SMALLINT     NOT NULL,
  PRIMARY KEY (student_id),
  UNIQUE KEY uq_students_roll (roll_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- INSTRUCTORS (FK to auth_db.users_auth.user_id)
CREATE TABLE instructors (
  instructor_id   BIGINT       NOT NULL,
  department      VARCHAR(16)  NOT NULL,
  instructor_name VARCHAR(128) NOT NULL,
  PRIMARY KEY (instructor_id),
  CONSTRAINT fk_instructor_auth
    FOREIGN KEY (instructor_id)
    REFERENCES auth_db.users_auth(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- COURSES (course_id like 'BIO101'; code = acronym like 'FOB')
CREATE TABLE courses (
  course_id VARCHAR(32)  NOT NULL,
  code      VARCHAR(16)  NOT NULL,
  title     VARCHAR(256) NOT NULL,
  credits   INT          NOT NULL,
  PRIMARY KEY (course_id),
  KEY idx_courses_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- SECTIONS
CREATE TABLE sections (
  section_id    INT NOT NULL AUTO_INCREMENT,
  course_id     VARCHAR(32) NOT NULL,
  instructor_id BIGINT      NOT NULL,
  day_time      VARCHAR(64) NOT NULL,
  room          VARCHAR(32) DEFAULT NULL,
  capacity      INT         NOT NULL,
  semester      VARCHAR(32) NOT NULL,
  year          INT         NOT NULL,
  PRIMARY KEY (section_id),
  KEY fk_section_course     (course_id),
  KEY fk_section_instructor (instructor_id),
  CONSTRAINT fk_section_course
    FOREIGN KEY (course_id)     REFERENCES courses(course_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_section_instructor
    FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ENROLLMENTS
CREATE TABLE enrollments (
  enrollment_id INT NOT NULL AUTO_INCREMENT,
  student_id    VARCHAR(64) NOT NULL,
  section_id    INT         NOT NULL,
  status ENUM('REGISTERED','DROPPED') NOT NULL DEFAULT 'REGISTERED',
  PRIMARY KEY (enrollment_id),
  UNIQUE KEY uq_student_section (student_id, section_id),
  KEY idx_enr_student (student_id),
  KEY idx_enr_section (section_id),
  CONSTRAINT fk_enr_student
    FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_enr_section
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- GRADES (one row per component; composite PK)
CREATE TABLE grades (
  enrollment_id INT NOT NULL,
  component     VARCHAR(64) NOT NULL, -- Quiz1, Midterm, EndSem, etc.
  score         DECIMAL(5,2) NULL,
  final_grade   VARCHAR(8)  NULL,     -- e.g., A, A-, B+
  PRIMARY KEY (enrollment_id, component),
  CONSTRAINT fk_grades_enr
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- SETTINGS
CREATE TABLE settings (
  `key`   VARCHAR(64)  NOT NULL,
  `value` VARCHAR(256) NULL,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ────────────────────────────────────────────────────────────────────────────
-- CSV IMPORTS  (expects CSVs in same directory; first row = headers)
-- If secure_file_priv blocks loads, run the mysql client with --local-infile=1
-- ────────────────────────────────────────────────────────────────────────────
SET SESSION sql_log_bin = 0;
SET GLOBAL local_infile = 1;

LOAD DATA LOCAL INFILE 'students.csv'
INTO TABLE students
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(student_id, roll_no, full_name, program, year);

LOAD DATA LOCAL INFILE 'instructors.csv'
INTO TABLE instructors
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(instructor_id, department, instructor_name);

LOAD DATA LOCAL INFILE 'courses.csv'
INTO TABLE courses
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(course_id, code, title, credits);

-- sections.csv may or may not include section_id; we handle blank → AUTO_INCREMENT
LOAD DATA LOCAL INFILE 'sections.csv'
INTO TABLE sections
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@sid, course_id, instructor_id, day_time, room, capacity, semester, year)
SET section_id = NULLIF(@sid,'');

-- enrollments.csv: enrollment_id (optional), student_id, section_id, status
LOAD DATA LOCAL INFILE 'enrollments.csv'
INTO TABLE enrollments
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@eid, student_id, section_id, @st)
SET enrollment_id = NULLIF(@eid,''),
    status        = IFNULL(NULLIF(@st,''),'REGISTERED');

-- grades.csv: enrollment_id, component, score, final_grade
LOAD DATA LOCAL INFILE 'grades.csv'
INTO TABLE grades
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(enrollment_id, component, @sc, @fg)
SET score = NULLIF(@sc,''), final_grade = NULLIF(@fg,'');

-- settings.csv: key,value
LOAD DATA LOCAL INFILE 'settings.csv'
INTO TABLE settings
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(`key`,`value`);

SET FOREIGN_KEY_CHECKS = 1;
