-- This migration script updates the well_boundaries table to allow NULL values for start_ms and end_ms columns.
ALTER TABLE well_boundaries
ALTER COLUMN start_ms DROP NOT NULL,
ALTER COLUMN end_ms DROP NOT NULL;