CREATE TABLE IF NOT EXISTS pharmacies (
    id SERIAL PRIMARY KEY,

    name varchar(200) NOT NULL,
    address text NOT NULL,
    city text,
    country text,

    phone_number varchar(32),
    alternate_phone_number varchar(32),

    website text,
    email varchar(320),

    latitude double precision,
    longitude double precision,

    description text,
    logo_url text,

    is_active boolean NOT NULL DEFAULT true,
    is_verified boolean NOT NULL DEFAULT false,

    licence_number varchar(255),
    admin_id bigint,

    average_rating double precision NOT NULL DEFAULT 0.0,
    total_reviews integer NOT NULL DEFAULT 0,

    create_at timestamp without time zone NOT NULL DEFAULT NOW(),
    update_at timestamp without time zone NOT NULL DEFAULT NOW()
);

-- Enable trigram extension for fuzzy search
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;

-- Pharmacies table with geospatial index
CREATE INDEX idx_pharmacies_location ON pharmacies USING gist(ll_to_earth(latitude, longitude));
CREATE INDEX idx_pharmacies_active ON pharmacies(is_active) WHERE is_active = true;
CREATE INDEX idx_pharmacies_admin ON pharmacies(admin_id);