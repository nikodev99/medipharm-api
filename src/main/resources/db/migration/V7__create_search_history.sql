CREATE TABLE search_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGSERIAL,
    searched_at TIMESTAMP NOT NULL DEFAULT NOW(),
    search_query VARCHAR(255) NOT NULL,
    result_count INT NOT NULL DEFAULT 0
);

-- Search history for analytics
CREATE INDEX idx_search_user ON search_history(user_id);
CREATE INDEX idx_search_date ON search_history(searched_at DESC);
CREATE INDEX idx_search_query ON search_history(search_query);