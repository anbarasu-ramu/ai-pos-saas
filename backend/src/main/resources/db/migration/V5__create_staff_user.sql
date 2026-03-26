CREATE TABLE staff_user (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    username VARCHAR(100),
    email VARCHAR(255),

    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT uk_staff_user_tenant UNIQUE (id, tenant_id)
);

-- ✅ separate statement
CREATE INDEX idx_staff_user_tenant ON staff_user(tenant_id);