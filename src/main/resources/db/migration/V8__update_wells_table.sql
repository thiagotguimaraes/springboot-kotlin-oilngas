-- Remove start_ms and end_ms columns from wells table
ALTER TABLE wells
DROP COLUMN IF EXISTS start_ms,
DROP COLUMN IF EXISTS end_ms;