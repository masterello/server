-- Migration to update chat table to support both general and task-specific chats

-- 1. Add new columns for chat types and timestamps
-- If table already has rows, add with DEFAULT first, then mark as NOT NULL
ALTER TABLE chat
    ADD COLUMN chat_type VARCHAR(20),
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Populate chat_type for existing rows
-- (Assume existing rows are GENERAL unless you know otherwise)
UPDATE chat
SET chat_type = 'GENERAL'
WHERE chat_type IS NULL;

-- Now enforce NOT NULL
ALTER TABLE chat ALTER COLUMN chat_type SET NOT NULL;

-- 2. Make task_id nullable to support general chats
ALTER TABLE chat ALTER COLUMN task_id DROP NOT NULL;

-- 3. Drop the old constraint
ALTER TABLE chat DROP CONSTRAINT IF EXISTS unique_task_worker;

-- 4. Add partial unique indexes instead of invalid partial constraints
-- Ensure only one general chat per user-worker pair
CREATE UNIQUE INDEX uk_user_worker_general_chat
    ON chat(user_id, worker_id, chat_type)
    WHERE chat_type = 'GENERAL';

-- Ensure only one task-specific chat per user-worker-task combination
CREATE UNIQUE INDEX uk_user_worker_task_chat
    ON chat(user_id, worker_id, task_id)
    WHERE task_id IS NOT NULL;

-- 5. Add check constraint to ensure business rules
ALTER TABLE chat ADD CONSTRAINT check_chat_type_task_id
    CHECK (
        (chat_type = 'GENERAL' AND task_id IS NULL) OR
        (chat_type = 'TASK_SPECIFIC' AND task_id IS NOT NULL)
    );

-- 6. Create indexes for better performance
CREATE INDEX idx_chat_participants
    ON public.chat(user_id, worker_id);

CREATE INDEX idx_chat_task_participants
    ON public.chat(user_id, worker_id, task_id)
    WHERE task_id IS NOT NULL;

-- =====================
-- Rollback section
-- =====================
--rollback DROP INDEX IF EXISTS idx_chat_worker_active;
--rollback DROP INDEX IF EXISTS idx_chat_user_active;
--rollback DROP INDEX IF EXISTS idx_chat_task_participants;
--rollback DROP INDEX IF EXISTS idx_chat_participants;
--rollback ALTER TABLE public.chat DROP CONSTRAINT IF EXISTS check_chat_type_task_id;
--rollback DROP INDEX IF EXISTS uk_user_worker_task_chat;
--rollback DROP INDEX IF EXISTS uk_user_worker_general_chat;
--rollback ALTER TABLE public.chat ALTER COLUMN task_id SET NOT NULL;
--rollback ALTER TABLE public.chat ADD CONSTRAINT unique_task_worker UNIQUE (task_id, worker_id);
--rollback ALTER TABLE public.chat DROP COLUMN IF EXISTS created_at;
--rollback ALTER TABLE public.chat DROP COLUMN IF EXISTS chat_type;
