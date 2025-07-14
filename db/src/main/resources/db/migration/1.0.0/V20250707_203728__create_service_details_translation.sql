 --migrations here
CREATE TABLE public.worker_service_details (
    worker_id UUID NOT NULL,
    service_id integer,
    language_code VARCHAR(16) NOT NULL,
    text TEXT NOT NULL,
    is_original BOOLEAN NOT NULL,

    CONSTRAINT pk_worker_worker_service_details PRIMARY KEY (worker_id, service_id, language_code)
);

CREATE INDEX idx_worker_service_details_worker_id ON public.worker_description(worker_id);

--rollback  -- Rollbacks here