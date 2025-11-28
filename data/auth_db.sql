-- =========================================
-- IIITD Academic ERP - Auth DB (CSV seed)
-- Uses data/users_auth.csv
-- =========================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS auth_db;
CREATE DATABASE auth_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE auth_db;

DROP TABLE IF EXISTS users_auth;

-- Matches your current schema (see screenshot)
CREATE TABLE users_auth (
  user_id         INT        NOT NULL,
  username        TEXT       NOT NULL,
  role            TEXT       NOT NULL,
  password_hash   TEXT       NOT NULL,
  status          TEXT       NOT NULL,
  last_login      TEXT       NULL,
  failed_attempts INT        NOT NULL DEFAULT 0,
  locked_until    DATETIME   NULL,
  PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Helpful indexes (optional, but good to have)
CREATE INDEX idx_users_auth_username ON users_auth (username(100));
CREATE INDEX idx_users_auth_role     ON users_auth (role(50));
CREATE INDEX idx_users_auth_status   ON users_auth (status(50));

SET SESSION sql_log_bin = 0;
-- If LOAD DATA LOCAL fails, run once as root:
--   SET GLOBAL local_infile = 1;

-- Expected CSV header:
-- user_id,username,role,password_hash,status,last_login,failed_attempts,locked_until
LOAD DATA LOCAL INFILE 'users_auth.csv'
INTO TABLE users_auth
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(user_id, username, role, password_hash, status,
 @last_login, failed_attempts, @locked_until)
SET last_login   = NULLIF(@last_login,''),
    locked_until = NULLIF(@locked_until,'');

SET FOREIGN_KEY_CHECKS = 1;
