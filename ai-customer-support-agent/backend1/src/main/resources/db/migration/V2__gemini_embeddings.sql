-- Safety Check: Fail with exception if legacy 1536-dimensional embeddings exist.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM document_chunks) THEN
        RAISE EXCEPTION 'Database contains legacy 1536-dimensional OpenAI embeddings. Because these are mathematically incompatible with Gemini embeddings, you must clear the database first. Please run "TRUNCATE TABLE documents CASCADE;" and then restart the application to apply migrations.';
    END IF;
END $$;

-- Drop existing index
DROP INDEX IF EXISTS idx_document_chunks_embedding;

-- Alter embedding column size to 768
ALTER TABLE document_chunks ALTER COLUMN embedding TYPE vector(768);

-- Recreate HNSW index for 768 dimensions
CREATE INDEX idx_document_chunks_embedding ON document_chunks USING hnsw (embedding vector_cosine_ops);
