-- ============================================
-- Add user tracking to pos_order
-- ============================================

-- 1. Add columns (nullable first to avoid failures on existing data)
ALTER TABLE pos_order
ADD COLUMN IF NOT EXISTS created_by_user_id UUID,
ADD COLUMN IF NOT EXISTS created_by_username VARCHAR(100);

-- 2. Backfill existing rows (only if any exist)
-- Using a default placeholder UUID (you can change if needed)
UPDATE pos_order
SET created_by_user_id = '00000000-0000-0000-0000-000000000000'
WHERE created_by_user_id IS NULL;

-- 3. Enforce NOT NULL constraint
ALTER TABLE pos_order
ALTER COLUMN created_by_user_id SET NOT NULL;

-- 4. Optional: enforce username not null (skip if unsure)
-- ALTER TABLE pos_order
-- ALTER COLUMN created_by_username SET NOT NULL;

-- 5. Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_pos_order_user ON pos_order(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_pos_order_tenant ON pos_order(tenant_id);