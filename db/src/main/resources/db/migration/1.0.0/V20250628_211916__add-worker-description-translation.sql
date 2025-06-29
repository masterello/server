 --migrations here

CREATE TABLE public.worker_description (
    worker_id UUID NOT NULL,
    language_code VARCHAR(16) NOT NULL,
    text TEXT NOT NULL,
    is_original BOOLEAN NOT NULL,

    CONSTRAINT pk_worker_description PRIMARY KEY (worker_id, language_code)
);

CREATE INDEX idx_worker_description_worker_id ON public.worker_description(worker_id);

--rollback  -- Rollbacks here