-- Enable pgvector and uuid extensions
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Table: tenants
CREATE TABLE tenants (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: documents
CREATE TABLE documents (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(50),
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    content_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tenant_content_hash UNIQUE (tenant_id, content_hash)
);

-- Table: document_chunks
CREATE TABLE document_chunks (
    id UUID PRIMARY KEY,
    document_id UUID GENERATED ALWAYS AS ((metadata->>'document_id')::uuid) STORED,
    tenant_id VARCHAR(50) GENERATED ALWAYS AS (metadata->>'tenant_id') STORED,
    category VARCHAR(50) GENERATED ALWAYS AS (metadata->>'category') STORED,
    chunk_index INT GENERATED ALWAYS AS ((metadata->>'chunk_index')::int) STORED,
    content TEXT NOT NULL,
    page_number INT GENERATED ALWAYS AS ((metadata->>'page_number')::int) STORED,
    embedding vector(1536) NOT NULL,
    metadata JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: conversations
CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: messages
CREATE TABLE messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    token_count INT NOT NULL,
    model VARCHAR(100),
    latency_ms BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: message_sources
CREATE TABLE message_sources (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    chunk_id UUID NOT NULL REFERENCES document_chunks(id) ON DELETE CASCADE,
    similarity_score DOUBLE PRECISION NOT NULL
);

-- Create standard indexes
CREATE INDEX idx_documents_tenant ON documents(tenant_id);
CREATE INDEX idx_document_chunks_document ON document_chunks(document_id);
CREATE INDEX idx_document_chunks_tenant ON document_chunks(tenant_id);
CREATE INDEX idx_document_chunks_category ON document_chunks(category);
CREATE INDEX idx_conversations_tenant ON conversations(tenant_id);
CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_message_sources_message ON message_sources(message_id);

-- GIN Index on metadata JSONB column for category/tenant/document filtering
CREATE INDEX idx_document_chunks_metadata ON document_chunks USING gin (metadata);

-- HNSW Vector Similarity Index on embedding column (using Cosine Distance <=> / vector_cosine_ops)
CREATE INDEX idx_document_chunks_embedding ON document_chunks USING hnsw (embedding vector_cosine_ops);
