-- ============================================================================
-- PayVault reference schema (PostgreSQL)
--
-- IMPORTANT DIFFERENCE FROM MYSQL: Postgres cannot auto-create a database from
-- a JDBC connection string the way MySQL can. You DO need to run the three
-- CREATE DATABASE lines below yourself, once, before starting the services.
-- Everything after that (the actual TABLES) is still created automatically
-- by Hibernate on first startup (ddl-auto=update) — you don't need to run the
-- CREATE TABLE statements below, they're just for reference.
--
-- HOW TO RUN JUST THE THREE CREATE DATABASE LINES:
--   Open a terminal and run: psql -U postgres
--   (enter your password when prompted, e.g. "root")
--   Then paste these three lines and press Enter:
--
--     CREATE DATABASE user_db;
--     CREATE DATABASE wallet_db;
--     CREATE DATABASE ledger_db;
--
--   Or do the same visually in pgAdmin: right-click "Databases" → Create →
--   Database, and create one each named user_db, wallet_db, ledger_db.
-- ============================================================================

-- Run these three once, manually, before starting any service:
-- CREATE DATABASE user_db;
-- CREATE DATABASE wallet_db;
-- CREATE DATABASE ledger_db;

-- ---------- user_db (User Service) ----------
-- \c user_db

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    pin_hash VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS otp_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN DEFAULT FALSE
);

-- ---------- wallet_db (Wallet Service) ----------
-- \c wallet_db

CREATE TABLE IF NOT EXISTS wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    version BIGINT DEFAULT 0,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS topup_requests (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- ---------- ledger_db (Transaction Service) ----------
-- \c ledger_db

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    category VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    balance_after DECIMAL(19,2),
    created_at TIMESTAMP NOT NULL
);
