CREATE TABLE IF NOT EXISTS location_sales_channels (
    location_id BIGINT NOT NULL REFERENCES location (id) ON DELETE CASCADE,
    sales_channel_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (location_id, sales_channel_id)
);

CREATE INDEX IF NOT EXISTS idx_location_sales_channels_channel
    ON location_sales_channels (sales_channel_id);

INSERT INTO location_sales_channels (location_id, sales_channel_id)
SELECT l.id, 'c0000000-0000-4000-8000-000000000002'
FROM location l
WHERE l.uuid = 'a0000000-0000-4000-8000-000000000003'
  AND NOT EXISTS (
      SELECT 1
      FROM location_sales_channels linked
      WHERE linked.location_id = l.id
        AND linked.sales_channel_id = 'c0000000-0000-4000-8000-000000000002'
  );
