-- Migration V6: Ensure unique indexes on documents, conversations, and messages

CREATE UNIQUE INDEX IF NOT EXISTS uq_documents_id ON documents(id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_conversations_id ON conversations(id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_messages_id ON messages(id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_tenants_id ON tenants(id);
