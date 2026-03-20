CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE tenant
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

UPDATE tenant
SET tenant_id = gen_random_uuid()
WHERE tenant_id IS NULL;

ALTER TABLE tenant
    ALTER COLUMN tenant_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_tenant_tenant_id ON tenant (tenant_id);
