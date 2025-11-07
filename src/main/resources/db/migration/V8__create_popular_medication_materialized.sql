-- Materialized view for popular medications
CREATE MATERIALIZED VIEW popular_medications AS
SELECT
    m.id,
    m.name,
    m.dci,
    COUNT(sh.id) as search_count,
    COUNT(DISTINCT pi.pharmacy_id) as available_pharmacies_count
FROM medications m
         LEFT JOIN search_history sh ON LOWER(sh.search_query) LIKE '%' || lower(m.name) || '%'
         LEFT JOIN pharmacy_inventories pi ON pi.medication_id = m.id AND pi.is_available = true
WHERE m.is_active = true
GROUP BY m.id, m.name, m.dci
ORDER BY search_count DESC;

-- Refresh materialized view daily via a cron job
CREATE INDEX idx_popular_meds_id ON popular_medications(id);

-- Statistics for query planner
ANALYZE users;
ANALYZE pharmacies;
ANALYZE medications;
ANALYZE pharmacy_inventories;
ANALYZE search_history;