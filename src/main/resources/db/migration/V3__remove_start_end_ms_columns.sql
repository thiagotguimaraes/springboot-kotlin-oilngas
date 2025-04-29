ALTER TABLE wells
DROP COLUMN IF EXISTS start_ms,
DROP COLUMN IF EXISTS end_ms;


DROP TRIGGER IF EXISTS update_well_boundaries ON timeseries_template;

DROP FUNCTION IF EXISTS update_well_boundaries;