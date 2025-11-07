CREATE TABLE IF NOT EXISTS medications(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_lowercase VARCHAR(100) NOT NULL,
    dci VARCHAR(100) NOT NULL,
    dci_lowercase VARCHAR(100) NOT NULL,
    description TEXT,
    dosage TEXT,
    form VARCHAR(32) NOT NULL, -- MedicationForm enum stocké en texte
    manufacturer VARCHAR(100),
    active_ingredients TEXT[], -- liste de chaînes
    image_urls TEXT[],         -- liste de chaînes
    leaflet_url TEXT,
    requires_prescription BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    search_count INTEGER NOT NULL DEFAULT 0,
    create_at TIMESTAMP NOT NULL DEFAULT NOW(),
    update_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Medications table with full-text search
-- Medications table with full-text search
CREATE INDEX idx_medications_name_lower ON medications(name_lowercase);
CREATE INDEX idx_medications_dci_lower ON medications(dci_lowercase);
CREATE INDEX idx_medications_search_count ON medications(search_count DESC);
CREATE INDEX idx_medications_trgm_name ON medications USING gin(name_lowercase gin_trgm_ops);
CREATE INDEX idx_medications_trgm_dci ON medications USING gin(dci_lowercase gin_trgm_ops);
