-- Remove review_required column from category table.
-- The review workflow has been replaced by a direct DRAFT → ACTIVE (publish) flow.
ALTER TABLE category DROP COLUMN IF EXISTS review_required;
