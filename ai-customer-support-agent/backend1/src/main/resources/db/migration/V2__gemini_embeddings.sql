-- Migration V2: Ensure Gemini 768-dimensional vector configuration and extensions
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Ensure embedding column is 768 dimensions for Gemini embeddings
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'document_chunks' AND column_name = 'embedding'
    ) THEN
        -- Drop legacy index if exists
        DROP INDEX IF EXISTS idx_document_chunks_embedding;
        
        -- Ensure column type is vector(768)
        ALTER TABLE document_chunks ALTER COLUMN embedding TYPE vector(768);
        
        -- Recreate HNSW Cosine distance index
        CREATE INDEX IF NOT EXISTS idx_document_chunks_embedding 
            ON document_chunks USING hnsw (embedding vector_cosine_ops);
    END IF;
END $$;
