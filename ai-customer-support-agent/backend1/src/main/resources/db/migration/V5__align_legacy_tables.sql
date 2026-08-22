-- Migration V5: Add missing columns to legacy documents and conversations tables

-- 1. Ensure columns on documents
ALTER TABLE documents ADD COLUMN IF NOT EXISTS title VARCHAR(255);
ALTER TABLE documents ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT 'general';
ALTER TABLE documents ADD COLUMN IF NOT EXISTS size_bytes BIGINT;
ALTER TABLE documents ADD COLUMN IF NOT EXISTS content_hash VARCHAR(64);
ALTER TABLE documents ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50) DEFAULT 'default';

DO $$
BEGIN
    -- Backfill title from filename if empty
    UPDATE documents SET title = filename WHERE title IS NULL AND filename IS NOT NULL;
    UPDATE documents SET title = 'Untitled Document' WHERE title IS NULL;
    
    -- Backfill size_bytes from size if available
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'documents' AND column_name = 'size'
    ) THEN
        UPDATE documents SET size_bytes = size WHERE size_bytes IS NULL;
    END IF;
    UPDATE documents SET size_bytes = 0 WHERE size_bytes IS NULL;
    
    -- Backfill content_hash
    UPDATE documents SET content_hash = md5(id::text || now()::text) WHERE content_hash IS NULL;
    
    -- Backfill tenant_id
    UPDATE documents SET tenant_id = 'default' WHERE tenant_id IS NULL;
END $$;

-- 2. Ensure columns on conversations
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS title VARCHAR(255) DEFAULT 'Support Session';
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50) DEFAULT 'default';
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS mode VARCHAR(20) DEFAULT 'AI';
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS assigned_agent VARCHAR(100);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS escalation_status VARCHAR(30) DEFAULT 'NONE';
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS escalation_reason TEXT;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS last_frustration_score INT DEFAULT 0;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS last_frustration_level VARCHAR(20) DEFAULT 'low';
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS last_sentiment VARCHAR(20) DEFAULT 'neutral';
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS last_emotion VARCHAR(30) DEFAULT 'neutral';
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS last_intent VARCHAR(50);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS last_domain VARCHAR(50);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS call_status VARCHAR(20) DEFAULT 'ACTIVE';

UPDATE conversations SET title = 'Support Session' WHERE title IS NULL;
UPDATE conversations SET tenant_id = 'default' WHERE tenant_id IS NULL;
