-- Denormalize last message fields for chat ordering and previews
ALTER TABLE chat ADD COLUMN IF NOT EXISTS last_message_at timestamptz NULL;
ALTER TABLE chat ADD COLUMN IF NOT EXISTS last_message_preview varchar(200) NULL;

-- Index to support keyset pagination and ordering by last_message_at desc, id desc
CREATE INDEX IF NOT EXISTS idx_chat_last_message ON chat (last_message_at DESC, id DESC);

