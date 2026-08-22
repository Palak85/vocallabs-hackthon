-- Migration V7: Ensure pgvector document_chunks table and message_sources table exist

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Table: document_chunks (Spring AI pgvector store)
CREATE TABLE IF NOT EXISTS document_chunks (
    id UUID PRIMARY KEY,
    document_id UUID GENERATED ALWAYS AS ((metadata->>'document_id')::uuid) STORED,
    tenant_id VARCHAR(50) GENERATED ALWAYS AS (metadata->>'tenant_id') STORED,
    category VARCHAR(50) GENERATED ALWAYS AS (metadata->>'category') STORED,
    chunk_index INT GENERATED ALWAYS AS ((metadata->>'chunk_index')::int) STORED,
    content TEXT NOT NULL,
    page_number INT GENERATED ALWAYS AS ((metadata->>'page_number')::int) STORED,
    embedding vector(768) NOT NULL,
    metadata JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_document_chunks_metadata ON document_chunks USING gin (metadata);
CREATE INDEX IF NOT EXISTS idx_document_chunks_embedding ON document_chunks USING hnsw (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_document_chunks_document ON document_chunks(document_id);
CREATE INDEX IF NOT EXISTS idx_document_chunks_tenant ON document_chunks(tenant_id);

-- Table: message_sources
CREATE TABLE IF NOT EXISTS message_sources (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    chunk_id UUID NOT NULL,
    similarity_score DOUBLE PRECISION NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_message_sources_message ON message_sources(message_id);
