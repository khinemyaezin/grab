CREATE TABLE IF NOT EXISTS merchant_member_authorities (
    member_id BIGINT NOT NULL,
    authority VARCHAR(64) NOT NULL,
    PRIMARY KEY (member_id, authority),
    CONSTRAINT fk_member_authorities_member FOREIGN KEY (member_id)
        REFERENCES merchant_members(id) ON DELETE CASCADE
);

CREATE INDEX idx_merchant_member_authorities_member ON merchant_member_authorities(member_id);
