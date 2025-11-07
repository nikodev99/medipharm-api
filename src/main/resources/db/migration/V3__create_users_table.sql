CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password TEXT,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(32),
    role VARCHAR(32) NOT NULL,              -- USER | PHARMACY_ADMIN | SUPER_ADMIN
    auth_provider VARCHAR(32) NOT NULL DEFAULT 'LOCAL', -- LOCAL | GOOGLE | FACEBOOK
    provider_id TEXT,
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,
    premium_expiry_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Users table with indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_provider ON users(auth_provider, provider_id);