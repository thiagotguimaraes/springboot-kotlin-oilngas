-- Create the well_boundaries table
CREATE TABLE well_boundaries (
    well_id UUID PRIMARY KEY,
    start_ms BIGINT,
    end_ms BIGINT,
    FOREIGN KEY (well_id) REFERENCES wells (id) ON DELETE CASCADE
);


-- Create the update_well_boundaries() function
CREATE OR REPLACE FUNCTION update_well_boundaries()
RETURNS TRIGGER AS $$
DECLARE
    current_start BIGINT;
    current_end BIGINT;
BEGIN
    -- Fetch current boundaries
    SELECT start_ms, end_ms INTO current_start, current_end
    FROM well_boundaries
    WHERE well_id = NEW.well_id;

    -- Update boundaries
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        UPDATE well_boundaries
        SET
            start_ms = LEAST(COALESCE(current_start, NEW.timestamp), NEW.timestamp),
            end_ms = GREATEST(COALESCE(current_end, NEW.timestamp), NEW.timestamp)
        WHERE well_id = NEW.well_id;
    ELSIF TG_OP = 'DELETE' THEN
        -- Recalculate boundaries if a row is deleted
        UPDATE well_boundaries
        SET
            start_ms = (SELECT MIN(timestamp) FROM timeseries_template WHERE well_id = OLD.well_id),
            end_ms = (SELECT MAX(timestamp) FROM timeseries_template WHERE well_id = OLD.well_id)
        WHERE well_id = OLD.well_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Attach the trigger to the timeseries_template table
CREATE TRIGGER update_well_boundaries_trigger
AFTER INSERT OR UPDATE OR DELETE ON timeseries_template
FOR EACH ROW EXECUTE FUNCTION update_well_boundaries();