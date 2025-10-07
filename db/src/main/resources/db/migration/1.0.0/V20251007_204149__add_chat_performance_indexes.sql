-- Performance optimization: Add indexes for chat message history queries
-- These indexes address 600-700ms query times by optimizing:
-- 1. Message history pagination (chat_id + created_at)
-- 2. Read receipt lookups (message_id + read_at filter)

-- ============================================================================
-- Optimize chat_message queries
-- ============================================================================

-- Drop the old single-column index if it exists (from V20250126_210615)
DROP INDEX IF EXISTS idx_message_chat_id;

-- Create composite index for efficient message history pagination
-- Covers: WHERE chat_id=? AND created_at<? ORDER BY created_at DESC
CREATE INDEX IF NOT EXISTS idx_message_chat_created 
    ON chat_message (chat_id, created_at);

-- ============================================================================
-- Optimize message_reads queries
-- ============================================================================

-- Partial index for read receipts: WHERE message_id IN (...) AND read_at IS NOT NULL
-- Complements existing partial indexes which only cover read_at IS NULL
CREATE INDEX IF NOT EXISTS idx_message_read_receipts
    ON message_reads (message_id)
    WHERE read_at IS NOT NULL;

-- ============================================================================
-- Expected Performance Impact:
-- - Message history query: 200-300ms reduction (eliminates full table scan)
-- - Read receipt query: 100-150ms reduction (optimizes IN clause with partial index)
-- - Eliminated COUNT query: 50-200ms saved (code-level optimization)
-- - Overall endpoint: 600-700ms → 50-100ms (6-10x improvement)
-- ============================================================================

-- rollback
-- DROP INDEX IF EXISTS idx_message_read_receipts;
-- DROP INDEX IF EXISTS idx_message_chat_created;
-- CREATE INDEX IF NOT EXISTS idx_message_chat_id ON chat_message(chat_id);
