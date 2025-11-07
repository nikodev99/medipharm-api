CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscription_plan VARCHAR(32) NOT NULL,         -- MONTHLY | YEARLY
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    transaction_id TEXT,
    payment_method VARCHAR(32),                      -- AIRTEL_MONEY | MTN_MONEY | CREDIT_CARD
    amount DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);