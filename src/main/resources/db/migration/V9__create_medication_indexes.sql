ALTER TABLE medications ADD COLUMN name_lowercase VARCHAR(100);
ALTER TABLE medications ADD COLUMN dci_lowercase VARCHAR(100);

CREATE INDEX idx_medications_name_lower ON medications(name_lowercase);
CREATE INDEX idx_medications_dci_lower ON medications(dci_lowercase);
CREATE INDEX idx_medications_trgm_name ON medications USING gin(name_lowercase gin_trgm_ops);
CREATE INDEX idx_medications_trgm_dci ON medications USING gin(dci_lowercase gin_trgm_ops);