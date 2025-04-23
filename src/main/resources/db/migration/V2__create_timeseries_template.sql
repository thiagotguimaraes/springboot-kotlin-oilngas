CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS timeseries_template (
    timestamp BIGINT PRIMARY KEY,
    pressure DOUBLE PRECISION,
    oil_rate DOUBLE PRECISION,
    temperature DOUBLE PRECISION
);

SELECT create_hypertable('timeseries_template', 'timestamp', if_not_exists => TRUE);

-- Optional: ensure trigger function exists
CREATE OR REPLACE FUNCTION update_well_boundaries()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE wells
  SET
    start_ms = LEAST(COALESCE(start_ms, NEW.timestamp), NEW.timestamp),
    end_ms = GREATEST(COALESCE(end_ms, NEW.timestamp), NEW.timestamp)
  WHERE collection = TG_TABLE_NAME;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
