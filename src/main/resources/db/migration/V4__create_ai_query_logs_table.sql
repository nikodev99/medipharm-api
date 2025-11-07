CREATE TABLE IF NOT EXISTS ai_query_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    medication_id BIGINT,
    query TEXT NOT NULL,
    response TEXT,
    tokens_used INTEGER NOT NULL DEFAULT 0,
    createdAt TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_aiql_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_aiql_med FOREIGN KEY (medication_id) REFERENCES medications(id) ON DELETE SET NULL
);