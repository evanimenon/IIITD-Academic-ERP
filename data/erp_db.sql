-- =========================================
-- IIITD Academic ERP - Main ERP DB (CSV seed)
-- Uses CSVs in ./data:
--   students.csv, instructors.csv, courses.csv,
--   sections.csv, section_components.csv,
--   enrollments.csv, grades.csv, settings.csv
-- =========================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS erp_db;
CREATE DATABASE erp_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE erp_db;

-- ─────────────────────────────────────────
-- STUDENTS
-- ─────────────────────────────────────────
CREATE TABLE students (
  student_id VARCHAR(64)  NOT NULL,
  roll_no    VARCHAR(32)  NOT NULL,
  full_name  VARCHAR(128) NULL,
  program    VARCHAR(64)  NOT NULL,
  year       SMALLINT     NOT NULL,
  PRIMARY KEY (student_id),
  UNIQUE KEY uq_students_roll (roll_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ─────────────────────────────────────────
-- INSTRUCTORS  (FK to auth_db.users_auth.user_id)
-- ─────────────────────────────────────────
CREATE TABLE instructors (
  instructor_id   BIGINT       NOT NULL,
  department      VARCHAR(32)  NOT NULL,
  instructor_name VARCHAR(128) NOT NULL,
  PRIMARY KEY (instructor_id),
  CONSTRAINT fk_instructor_auth
    FOREIGN KEY (instructor_id)
    REFERENCES auth_db.users_auth(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ─────────────────────────────────────────
-- COURSES
-- ─────────────────────────────────────────
CREATE TABLE courses (
  course_id VARCHAR(32)  NOT NULL,
  code      VARCHAR(16)  NOT NULL,
  title     VARCHAR(256) NOT NULL,
  credits   INT          NOT NULL,
  PRIMARY KEY (course_id),
  KEY idx_courses_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ─────────────────────────────────────────
-- SECTIONS
-- ─────────────────────────────────────────
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

-- ─────────────────────────────────────────
-- SECTION_COMPONENTS  (***id*** is the PK)
-- CSV: id,section_id,component_name,weight
-- ─────────────────────────────────────────
CREATE TABLE section_components (
  id             INT NOT NULL AUTO_INCREMENT,
  section_id     INT NOT NULL,
  component_name TEXT NOT NULL,
  weight         INT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_seccomp_section (section_id),
  CONSTRAINT fk_seccomp_section
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ─────────────────────────────────────────
-- ENROLLMENTS
-- ─────────────────────────────────────────
CREATE TABLE enrollments (
  enrollment_id INT NOT NULL AUTO_INCREMENT,
  student_id    VARCHAR(64) NOT NULL,
  section_id    INT         NOT NULL,
  status        ENUM('REGISTERED','DROPPED') NOT NULL DEFAULT 'REGISTERED',
  final_grade   VARCHAR(8)  NULL,
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

-- ─────────────────────────────────────────
-- GRADES
-- CSV: grade_id,enrollment_id,component_id,score
-- component_id here refers to section_components.id
-- ─────────────────────────────────────────
CREATE TABLE grades (
  grade_id      INT NOT NULL AUTO_INCREMENT,
  enrollment_id INT NOT NULL,
  component_id  INT NOT NULL,
  score         DOUBLE NULL,
  PRIMARY KEY (grade_id),
  KEY idx_grades_enrollment (enrollment_id),
  KEY idx_grades_component  (component_id),
  CONSTRAINT fk_grades_enrollment
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_grades_component
    FOREIGN KEY (component_id)  REFERENCES section_components(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ─────────────────────────────────────────
-- SETTINGS  (CSV: setting_key,setting_value)
-- ─────────────────────────────────────────
CREATE TABLE settings (
  setting_key   VARCHAR(64)  NOT NULL,
  setting_value VARCHAR(256) NULL,
  PRIMARY KEY (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ─────────────────────────────────────────
-- CSV IMPORTS
-- ─────────────────────────────────────────
SET SESSION sql_log_bin = 0;
-- If needed once as root:  SET GLOBAL local_infile = 1;

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

LOAD DATA LOCAL INFILE 'sections.csv'
INTO TABLE sections
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@sid, course_id, instructor_id, day_time, room, capacity, semester, year)
SET section_id = NULLIF(@sid,'');

LOAD DATA LOCAL INFILE 'section_components.csv'
INTO TABLE section_components
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(id, section_id, component_name, weight);

LOAD DATA LOCAL INFILE 'enrollments.csv'
INTO TABLE enrollments
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@eid, student_id, section_id, @st, @fg)
SET enrollment_id = NULLIF(@eid,''),
    status        = IFNULL(NULLIF(@st,''),'REGISTERED'),
    final_grade   = NULLIF(@fg,'');

LOAD DATA LOCAL INFILE 'grades.csv'
INTO TABLE grades
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(grade_id, enrollment_id, component_id, score);

LOAD DATA LOCAL INFILE 'settings.csv'
INTO TABLE settings
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(setting_key, setting_value);

SET FOREIGN_KEY_CHECKS = 1;
