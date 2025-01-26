 --migrations here
CREATE TABLE public.chat (
    id UUID NOT NULL PRIMARY KEY,
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    worker_id UUID NOT NULL,

    CONSTRAINT unique_task_worker UNIQUE (task_id, worker_id)
);

CREATE TABLE chat_message (
    id UUID PRIMARY KEY,
    chat_id UUID NOT NULL,
    message TEXT NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_chat_id ON chat_message(chat_id);


--rollback  -- Rollbacks here