CREATE INDEX IF NOT EXISTS idx_product_tenant ON product (tenant_id);
CREATE INDEX IF NOT EXISTS idx_order_tenant ON pos_order (tenant_id);