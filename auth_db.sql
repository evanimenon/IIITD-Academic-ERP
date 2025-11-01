CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE users_auth (
  user_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(64)  NOT NULL UNIQUE,
  role          ENUM('admin','instructor','student') NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  status        ENUM('active','inactive') DEFAULT 'active',
  last_login    DATETIME NULL
);

-- Temporary sample users (we will replace passwords later with hashed ones)
INSERT INTO users_auth (username, role, password_hash) VALUES
('admin1','admin','temp123'),
('inst1','instructor','temp123'),
('stu1','student','temp123');
users_auth