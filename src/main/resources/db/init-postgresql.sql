-- ============================================================
-- PostgreSQL Database Initialization Script for Gym CRM
-- ============================================================
-- Run this script to create the database and user
-- ============================================================

-- Create database (run as postgres superuser)
CREATE DATABASE gymdb
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Connect to the database
\c gymdb;

-- Create schema (optional, Hibernate will create tables)
CREATE SCHEMA IF NOT EXISTS gym;

-- Add comment
COMMENT ON DATABASE gymdb IS 'Gym CRM Database for Hibernate Project';
