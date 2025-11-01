CREATE DATABASE IF NOT EXISTS erp_db;
USE erp_db;

-- STUDENTS (link to auth_db users_auth.user_id)
CREATE TABLE students (
  student_id BIGINT PRIMARY KEY,
  roll_no VARCHAR(32) NOT NULL UNIQUE,
  program VARCHAR(64) NOT NULL,
  year SMALLINT NOT NULL,
  FOREIGN KEY (student_id) REFERENCES auth_db.users_auth(user_id)
);

-- INSTRUCTORS (link to auth_db)
CREATE TABLE instructors (
  instructor_id BIGINT PRIMARY KEY,
  department VARCHAR(64) NOT NULL,
  FOREIGN KEY (instructor_id) REFERENCES auth_db.users_auth(user_id)
);

-- COURSES (IIIT-style list)
CREATE TABLE courses (
  course_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(16) NOT NULL UNIQUE,
  title VARCHAR(128) NOT NULL,
  credits DECIMAL(3,1) NOT NULL
);

-- SECTIONS (each course taught by an instructor)
CREATE TABLE sections (
  section_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  instructor_id BIGINT NOT NULL,
  day_time VARCHAR(64) NOT NULL,
  room VARCHAR(32) NOT NULL,
  capacity INT NOT NULL,
  semester VARCHAR(16) NOT NULL,
  year SMALLINT NOT NULL,
  FOREIGN KEY (course_id) REFERENCES courses(course_id),
  FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id)
);

-- Example instructors (these IDs MUST match users in auth_db)
-- Assume: admin1=1, inst1=2, inst2=3, stu1=4, stu2=5, stu3=6
INSERT INTO instructors VALUES
(2, 'CSE'),
(3, 'CSE');

-- SECTIONS for Spring 2026
INSERT INTO sections(course_id, instructor_id, day_time, room, capacity, semester, year) VALUES
(1, 2, 'Mon/Wed 10:00-11:30', 'R101', 40, 'Spring', 2026), -- DSA by inst1
(2, 3, 'Tue/Thu 09:00-10:30', 'R202', 40, 'Spring', 2026), -- IP by inst2
(3, 2, 'Mon/Wed 14:00-15:30', 'R105', 35, 'Spring', 2026), -- OS by inst1
(4, 3, 'Tue/Thu 12:00-13:30', 'R301', 35, 'Spring', 2026), -- DBMS by inst2
(5, 2, 'Fri 10:00-12:00', 'R210', 50, 'Spring', 2026),     -- Discrete Math
(6, 3, 'Wed 16:00-17:00', 'R120', 60, 'Spring', 2026);     -- Communication Skills

-- STUDENTS (link to auth_db users_auth.user_id)
INSERT INTO students VALUES
(4, 'MT202501', 'B.Tech CSE', 1),
(5, 'MT202502', 'B.Tech CSE', 1),
(6, 'MT202503', 'B.Tech CSE', 1);

-- ENROLLMENTS (students registering for courses)
CREATE TABLE enrollments (
  enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  section_id BIGINT NOT NULL,
  status ENUM('REGISTERED','DROPPED','COMPLETED') DEFAULT 'REGISTERED',
  UNIQUE (student_id, section_id),
  FOREIGN KEY (student_id) REFERENCES students(student_id),
  FOREIGN KEY (section_id) REFERENCES sections(section_id)
);

INSERT INTO enrollments(student_id, section_id) VALUES
(4,1),(4,2),(4,3),(4,5),
(5,1),(5,4),(5,6),
(6,2),(6,3),(6,5),(6,6);

-- GRADES (per-assessment coinstructorsmponent)
CREATE TABLE grades (
  grade_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  enrollment_id BIGINT NOT NULL,
  component VARCHAR(32) NOT NULL,
  score DECIMAL(6,2) NOT NULL,
  final_grade VARCHAR(4),
  UNIQUE (enrollment_id, component),
  FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);

-- SETTINGS (maintenance mode toggle)
CREATE TABLE settings (
  `key` VARCHAR(64) PRIMARY KEY,
  `value` VARCHAR(64) NOT NULL
);

INSERT INTO settings VALUES ('maintenance_on','false');
