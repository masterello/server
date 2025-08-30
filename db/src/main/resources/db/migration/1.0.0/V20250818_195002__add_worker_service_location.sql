 --migrations here
-- Add 'online' column to main worker_info table
ALTER TABLE public.worker_info
    ALTER COLUMN city DROP NOT NULL;

ALTER TABLE worker_info
    ADD COLUMN online BOOLEAN DEFAULT FALSE;

-- Create table for multiple cities per worker
CREATE TABLE public.worker_location_cities (
    worker_id UUID NOT NULL,
    city VARCHAR(255) NOT NULL,
    CONSTRAINT fk_worker_location_cities_worker
        FOREIGN KEY (worker_id)
        REFERENCES worker_info(worker_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_worker_location_cities_city
    ON public.worker_location_cities(city);

CREATE INDEX idx_worker_info_online
    ON public.worker_info(online);

-- Data migration: copy existing city values into the new table
INSERT INTO public.worker_location_cities (worker_id, city)
SELECT worker_id, city
FROM worker_info
WHERE city IS NOT NULL
  AND city <> '';
--rollback  -- Rollbacks here