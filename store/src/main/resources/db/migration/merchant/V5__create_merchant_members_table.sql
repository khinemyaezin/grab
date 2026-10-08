CREATE TABLE merchant_members (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    merchant_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    role VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    invited_by VARCHAR(255),
    invitation_expires_at TIMESTAMP WITH TIME ZONE,
    joined_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_merchant_member UNIQUE (merchant_id, user_id)
);

CREATE INDEX idx_merchant_members_merchant ON merchant_members(merchant_id);
CREATE INDEX idx_merchant_members_user ON merchant_members(user_id);
CREATE INDEX idx_merchant_members_status ON merchant_members(status);
