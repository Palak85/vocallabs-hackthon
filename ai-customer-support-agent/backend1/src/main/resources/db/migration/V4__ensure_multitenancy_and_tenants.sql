-- Migration V4: Ensure Multi-Tenant Structures (Tenants Table and tenant_id Foreign Keys)

-- 1. Create tenants table if missing
CREATE TABLE IF NOT EXISTS tenants (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Seed default tenant
INSERT INTO tenants (id, name) VALUES ('default', 'Default Tenant') ON CONFLICT (id) DO NOTHING;

-- 3. Ensure tenant_id column in documents table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'documents' AND column_name = 'tenant_id'
    ) THEN
        ALTER TABLE documents ADD COLUMN tenant_id VARCHAR(50) DEFAULT 'default';
        UPDATE documents SET tenant_id = 'default' WHERE tenant_id IS NULL;
        ALTER TABLE documents ALTER COLUMN tenant_id SET NOT NULL;
        ALTER TABLE documents ADD CONSTRAINT fk_documents_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
    END IF;
END $$;

-- 4. Ensure tenant_id column in conversations table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'conversations' AND column_name = 'tenant_id'
    ) THEN
        ALTER TABLE conversations ADD COLUMN tenant_id VARCHAR(50) DEFAULT 'default';
        UPDATE conversations SET tenant_id = 'default' WHERE tenant_id IS NULL;
        ALTER TABLE conversations ALTER COLUMN tenant_id SET NOT NULL;
        ALTER TABLE conversations ADD CONSTRAINT fk_conversations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
    END IF;
END $$;

-- 5. Ensure indexes exist
CREATE INDEX IF NOT EXISTS idx_documents_tenant ON documents(tenant_id);
CREATE INDEX IF NOT EXISTS idx_conversations_tenant ON conversations(tenant_id);
