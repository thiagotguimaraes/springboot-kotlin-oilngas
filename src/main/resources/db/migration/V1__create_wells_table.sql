CREATE TABLE wells (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    collection TEXT NOT NULL,
    start_ms BIGINT,
    end_ms BIGINT
);