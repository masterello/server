-- Creates per-message per-recipient read status with read_at timestamp
-- Allows precise unread counts and read receipts in 1:1 (and extensible to groups)

CREATE TABLE IF NOT EXISTS message_reads (
  message_id    UUID      NOT NULL REFERENCES chat_message(id) ON DELETE CASCADE,
  chat_id       UUID      NOT NULL,
  recipient_id  UUID      NOT NULL,
  read_at       TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT pk_message_reads PRIMARY KEY (message_id, recipient_id)
);

-- Hot paths: unread by user, unread by user per chat
CREATE INDEX IF NOT EXISTS idx_mr_recipient_unread
  ON message_reads (recipient_id)
  WHERE read_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_mr_recipient_chat_unread
  ON message_reads (recipient_id, chat_id)
  WHERE read_at IS NULL;

-- Optional: for joins/analytics
CREATE INDEX IF NOT EXISTS idx_mr_chat
  ON message_reads (chat_id);

-- rollback
-- DROP INDEX IF EXISTS idx_mr_chat;
-- DROP INDEX IF EXISTS idx_mr_recipient_chat_unread;
-- DROP INDEX IF EXISTS idx_mr_recipient_unread;
-- DROP TABLE IF EXISTS message_reads;