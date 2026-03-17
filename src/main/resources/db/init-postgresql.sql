-- Drop database if exists (optional - for clean start)
DROP DATABASE IF EXISTS gymdb;

-- Create database
CREATE DATABASE gymdb
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Add comment
COMMENT ON DATABASE gymdb IS 'Gym CRM Database for Hibernate Project';

-- Connect to the database
\c gymdb;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE gymdb TO postgres;

-- Create extensions (optional but useful)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Success message
SELECT 'Database gymdb created successfully!' AS message;