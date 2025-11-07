CREATE TABLE IF NOT EXISTS pharmacy_inventories (
    id BIGSERIAL PRIMARY KEY,
    pharmacy_id BIGINT NOT NULL REFERENCES pharmacies(id) ON DELETE CASCADE,
    medication_id BIGINT NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    quantity INTEGER,
    price DOUBLE PRECISION CHECK (price >= 0),
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    expiry_date TIMESTAMP,
    last_updated TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_inventory_pharmacy_medication UNIQUE (pharmacy_id, medication_id)
);

-- Pharmacy inventory with composite indexes
CREATE INDEX idx_inventory_pharmacy ON pharmacy_inventories(pharmacy_id);
CREATE INDEX idx_inventory_medication ON pharmacy_inventories(medication_id);
CREATE INDEX idx_inventory_available ON pharmacy_inventories(medication_id, is_available)
    WHERE is_available = true AND quantity > 0;
CREATE UNIQUE INDEX idx_inventory_unique ON pharmacy_inventories(pharmacy_id, medication_id);