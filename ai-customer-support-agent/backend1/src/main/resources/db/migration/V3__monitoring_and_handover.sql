-- Migration V3: Add Monitoring, Escalation, and Human Handover Columns to Conversations

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS mode VARCHAR(20) DEFAULT 'AI',
    ADD COLUMN IF NOT EXISTS assigned_agent VARCHAR(100),
    ADD COLUMN IF NOT EXISTS escalation_status VARCHAR(30) DEFAULT 'NONE',
    ADD COLUMN IF NOT EXISTS escalation_reason TEXT,
    ADD COLUMN IF NOT EXISTS last_frustration_score INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_frustration_level VARCHAR(20) DEFAULT 'low',
    ADD COLUMN IF NOT EXISTS last_sentiment VARCHAR(20) DEFAULT 'neutral',
    ADD COLUMN IF NOT EXISTS last_emotion VARCHAR(30) DEFAULT 'neutral',
    ADD COLUMN IF NOT EXISTS last_intent VARCHAR(50),
    ADD COLUMN IF NOT EXISTS last_domain VARCHAR(50),
    ADD COLUMN IF NOT EXISTS call_status VARCHAR(20) DEFAULT 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_conversations_mode ON conversations(mode);
CREATE INDEX IF NOT EXISTS idx_conversations_escalation ON conversations(escalation_status);
