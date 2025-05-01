-- This script updates the well_boundaries table with the start and end timestamps of each well timeseries hyper table
DO $$
DECLARE
    well RECORD;
    start_ms BIGINT;
    end_ms BIGINT;
    hypertable_name TEXT;
BEGIN
    -- Loop through each well
    FOR well IN SELECT id, collection FROM wells LOOP
        -- Dynamically construct the hypertable name
        hypertable_name := well.collection;

        -- Check if the hypertable exists
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = hypertable_name) THEN
            -- Calculate MIN and MAX timestamps for the hypertable
            EXECUTE format('SELECT MIN(timestamp), MAX(timestamp) FROM %I', hypertable_name)
            INTO start_ms, end_ms;

            -- Insert the boundaries into the well_boundaries table
            INSERT INTO well_boundaries (well_id, start_ms, end_ms)
            VALUES (well.id, start_ms, end_ms)
            ON CONFLICT (well_id) DO UPDATE
            SET start_ms = EXCLUDED.start_ms,
                end_ms = EXCLUDED.end_ms;
        END IF;
    END LOOP;
END $$;