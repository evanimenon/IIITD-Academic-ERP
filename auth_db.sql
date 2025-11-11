-- =========================================
-- IIITD Academic ERP - Demo Auth Database
-- Creates schema + users_auth table + demo rows (with BCRYPT hashes)
-- Demo credentials (case sensitive):
--   Admins:      admin1           / admin@123
--   Instructors: inst1, sambuddho / inst@123
--                 inst3, payel, shad, ravi
--   Students:    stu1, stu2       / stud@123
-- =========================================

-- (Optional) Create MySQL app user expected by the app:
-- Uncomment and run with a privileged MySQL account if needed.
-- CREATE USER IF NOT EXISTS 'auth_user'@'localhost' IDENTIFIED BY 'secret';
-- GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_user'@'localhost';
-- FLUSH PRIVILEGES;

-- Create database
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

-- Recreate table for a clean demo (safe for demo environments)
DROP TABLE IF EXISTS users_auth;

CREATE TABLE users_auth (
  user_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(64)  NOT NULL UNIQUE,
  role          ENUM('admin','instructor','student') NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  status        ENUM('active','inactive') DEFAULT 'active',
  last_login    DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------
-- Demo users (bcrypt-hashed passwords)
-- Hashes generated via BCrypt (work factor 12)
-- admin@123 → $2a$12$QWyylCqkkii8JOqGl14G8eVKXe8LDBuqgcEblQ1wc8hPJN029Js.K
-- inst@123  → $2a$12$GMXaY1JgfkJ6OOGqDxDdVOIFzWARr2kBiSLD7ltqxTk8qlyBhFax.
-- stud@123  → $2a$12$XAoENJtYORZ/FfXiXJoan.IjqLX9MWncTJ69obWn/VOFDuUkJUY5e
-- -----------------------------------------

INSERT INTO users_auth (username, role, password_hash, status) VALUES
-- Admins
('admin1','admin','$2a$12$QWyylCqkkii8JOqGl14G8eVKXe8LDBuqgcEblQ1wc8hPJN029Js.K','active'),

-- Instructors
('inst1','instructor',     '$2a$12$GMXaY1JgfkJ6OOGqDxDdVOIFzWARr2kBiSLD7ltqxTk8qlyBhFax.','active'),
('sambuddho','instructor', '$2a$12$GMXaY1JgfkJ6OOGqDxDdVOIFzWARr2kBiSLD7ltqxTk8qlyBhFax.','active'),
('inst3','instructor',     '$2a$12$GMXaY1JgfkJ6OOGqDxDdVOIFzWARr2kBiSLD7ltqxTk8qlyBhFax.','active'),
('payel','instructor',     '$2a$12$GMXaY1JgfkJ6OOGqDxDdVOIFzWARr2kBiSLD7ltqxTk8qlyBhFax.','active'),
('shad','instructor',      '$2a$12$GMXaY1JgfkJ6OOGqDxDdVOIFzWARr2kBiSLD7ltqxTk8qlyBhFax.','active'),
('ravi','instructor',      '$2a$12$GMXaY1JgfkJ6OOGqDxDdVOIFzWARr2kBiSLD7ltqxTk8qlyBhFax.','active'),

-- Students
('stu1','student','$2a$12$XAoENJtYORZ/FfXiXJoan.IjqLX9MWncTJ69obWn/VOFDuUkJUY5e','active'),
('stu2','student','$2a$12$XAoENJtYORZ/FfXiXJoan.IjqLX9MWncTJ69obWn/VOFDuUkJUY5e','active');

-- Helpful indexes
CREATE INDEX idx_users_auth_role ON users_auth(role);
CREATE INDEX idx_users_auth_status ON users_auth(status);

-- Quick verification queries (optional)
-- SELECT username, role, status, LEFT(password_hash,4) AS pfx, LENGTH(password_hash) AS len FROM users_auth;
-- Expected: pfx = '$2a' (or '$2b') and len ≈ 60
