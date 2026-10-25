-- Local Development Seed: Default User (a@a.com / 123123123)
INSERT INTO users (
    id,
    uuid,
    email,
    password_hash,
    status,
    authentication_version,
    created_at,
    updated_at
) VALUES (
    1,
    '00000000-0000-0000-0000-000000000001',
    'a@a.com',
    '$2a$10$qLQVXz0fklM5anflOYPtrukGde46NCbYMgkxFrQy/0nXb4I1X8GP2',
    'ACTIVE',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;
